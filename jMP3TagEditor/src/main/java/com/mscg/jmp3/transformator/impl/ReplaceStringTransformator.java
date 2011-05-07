package com.mscg.jmp3.transformator.impl;

import java.security.InvalidParameterException;

import com.mscg.i18n.Messages;
import com.mscg.jmp3.transformator.StringTransformator;

public class ReplaceStringTransformator implements StringTransformator {

    private static final long serialVersionUID = 6511923368765816773L;

    private String origString;
    private String replaceString;

    @Override
    public String getName() {
        return Messages.getString("transform.string.replace.name");
    }

    @Override
    public String[] getParametersNames() {
        return new String[]{Messages.getString("transform.string.replace.param.orig"),
                            Messages.getString("transform.string.replace.param.replace")};
    }

    @Override
    public void setParameter(int index, String parameter) throws InvalidParameterException {
        switch(index) {
        case 0:
            if(parameter == null || parameter.trim().length() == 0)
                throw new InvalidParameterException(this.getClass().getSimpleName() +
                                                    " requires that the original string must not be empty");
            origString = parameter;
            break;
        case 1:
            if(parameter == null || parameter.trim().length() == 0)
                throw new InvalidParameterException(this.getClass().getSimpleName() +
                                                    " requires that the replace string must not be empty");
            replaceString = parameter;
            break;
        default:
            throw new InvalidParameterException(this.getClass().getSimpleName() +
                                                " requires exactly 2 parameters.");
        }
    }

    @Override
    public String transformString(String orig) {
        return orig.replace(origString, replaceString);
    }

}
