/*
 * Copyright © 2018 Apollo Foundation
 */

package com.apollocurrency.aplwallet.apl;

import com.apollocurrency.aplwallet.apl.util.Convert;
import org.json.simple.JSONObject;

import java.util.Objects;


public class GeneratedAccount {
    private long id;
    private byte[] publicKey;
    private byte[] privateKey;
    private String passphrase;
    private byte[] keySeed;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GeneratedAccount)) return false;
        GeneratedAccount that = (GeneratedAccount) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = Convert.parseHexString(publicKey);
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(byte[] privateKey) {
        this.privateKey = privateKey;
    }
    public void setPrivateKey(String privateKey) {
        this.privateKey = Convert.parseHexString(privateKey);
    }

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    public GeneratedAccount(long id, byte[] publicKey, byte[] privateKey, String passphrase, byte[] keySeed) {
        this.id = id;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.passphrase = passphrase;
        this.keySeed = keySeed;
    }

    public GeneratedAccount() {
    }

    public void setKeySeed(byte[] keySeed) {
        this.keySeed = keySeed;
    }

    public byte[] getKeySeed() {
        return keySeed;
    }

    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
        jsonObject.put("rsAddress", Convert.rsAccount(id));
        if (publicKey != null) {
            jsonObject.put("publicKey", Convert.toHexString(publicKey));
        }
        if (passphrase != null) {
            jsonObject.put("passphrase", passphrase);
        }
        return jsonObject;
    }

}
