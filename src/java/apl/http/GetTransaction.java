/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2017 Jelurida IP B.V.
 * Copyright © 2018 Apollo Foundation
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Apollo Foundation B.V.,
 * no part of the Apl software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package apl.http;

import apl.Apl;
import apl.Transaction;
import apl.TransactionType;
import apl.util.Convert;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static apl.http.JSONResponses.*;

public final class GetTransaction extends APIServlet.APIRequestHandler {

    static final GetTransaction instance = new GetTransaction();

    private GetTransaction() {
        super(new APITag[] {APITag.TRANSACTIONS}, "transaction", "fullHash", "includePhasingResult");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) {

        String transactionIdString = Convert.emptyToNull(req.getParameter("transaction"));
        String transactionFullHash = Convert.emptyToNull(req.getParameter("fullHash"));
        if (transactionIdString == null && transactionFullHash == null) {
            return MISSING_TRANSACTION;
        }
        boolean includePhasingResult = "true".equalsIgnoreCase(req.getParameter("includePhasingResult"));

        long transactionId = 0;
        Transaction transaction;
        try {
            if (transactionIdString != null) {
                transactionId = Convert.parseUnsignedLong(transactionIdString);
                transaction = Apl.getBlockchain().getTransaction(transactionId);
            } else {
                transaction = Apl.getBlockchain().getTransactionByFullHash(transactionFullHash);
                if (transaction == null) {
                    return UNKNOWN_TRANSACTION;
                }
            }
        } catch (RuntimeException e) {
            return INCORRECT_TRANSACTION;
        }
        if (transaction == null) {
            transaction = Apl.getTransactionProcessor().getUnconfirmedTransaction(transactionId);
            if (transaction == null || transaction.getType() == TransactionType.Payment.PRIVATE) {
                return UNKNOWN_TRANSACTION;
            }
            return JSONData.unconfirmedTransaction(transaction);
        } else {
            if (transaction.getType() == TransactionType.Payment.PRIVATE) {
                return UNKNOWN_TRANSACTION;
            }
            return JSONData.transaction(transaction, includePhasingResult, false);
        }

    }

}
