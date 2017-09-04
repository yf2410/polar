package com.polar.browser.utils.ahocorasick;

public class MatchToken extends Token {

    private Emit emit;

    public MatchToken(String fragment, Emit emit) {
        super(fragment);
        this.emit = emit;
    }

    @Override
    public boolean isMatch() {
        return true;
    }

    @Override
    public Emit getEmit() {
        return this.emit;
    }

}
