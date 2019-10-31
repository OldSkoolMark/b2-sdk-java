/*
 * Copyright 2017, Backblaze Inc. All Rights Reserved.
 * License https://www.backblaze.com/using_b2_code.html
 */
package com.backblaze.b2.client;

import com.backblaze.b2.client.exceptions.B2Exception;
import com.backblaze.b2.client.exceptions.B2InternalErrorException;
import com.backblaze.b2.client.structures.B2AccountAuthorization;

/**
 * B2AccountAuthorizerV5AuthImpl is used when the user has authenticated
 * with B1.
 */
public class B2AccountAuthorizerV5AuthImpl implements B2AccountAuthorizer {
    private final B2AccountAuthorization b2AccountAuthorization;

    @Override
    public B2AccountAuthorization authorize(B2StorageClientWebifier webifier) throws B2Exception{
        if( b2AccountAuthorization == null ){ // if Java is broken
            throw new B2InternalErrorException("constructor somehow failed");
        }
        return b2AccountAuthorization;
    }

    public B2AccountAuthorizerV5AuthImpl(B2AccountAuthorization b2AccountAuthorization){
        this.b2AccountAuthorization = b2AccountAuthorization;
    }
}
