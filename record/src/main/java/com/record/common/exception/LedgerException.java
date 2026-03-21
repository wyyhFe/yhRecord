package com.record.common.exception;

public class LedgerException extends BusinessException {

    public LedgerException(String message) {
        super(ErrorCode.LEDGER_ERROR, message);
    }
}

