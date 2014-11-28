package com.mapgen;

import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;

/**
 * A specialised NumberFormat which handles null values and empty Strings. 
 * This is useful in cell editors and used in StrictNumberFormatter.
 * 
 * @author Noel Grandin
 * @author Jeanette Winzenburg
 */
@SuppressWarnings("serial")
public class DecimalFormatExt extends  DecimalFormat {
    
    private  DecimalFormat childFormat;

    public  DecimalFormatExt() {
        this(null);
    }
    
    public  DecimalFormatExt( DecimalFormat childFormat) {
        if (childFormat == null) {
            childFormat =  new DecimalFormat();
        }
        this.childFormat = childFormat;
    }

    @Override
    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
        if (obj == null)
            return new AttributedString("").getIterator();
        return childFormat.formatToCharacterIterator(obj);
    }

    /*
    @Override
    public StringBuffer format(Object obj, StringBuffer toAppendTo,
            FieldPosition pos) {
        if (obj == null)
            return new StringBuffer("");
        return childFormat.format(obj, toAppendTo, pos);
    }
    */

    @Override
    public Number parse(String source, ParsePosition pos) {
        if (source == null) {
            pos.setIndex(1); // otherwise Format thinks parse failed
            return null;
        }
        if (source.trim().equals("")) {
            pos.setIndex(1); // otherwise Format thinks parse failed
            return 0;		// turn to 0, don't return null otherwise compareTo gets Null pointer exception
        }
        Number val = childFormat.parse(source, pos);
        /*
         * The default behaviour of Format objects is to keep parsing as long as
         * they encounter valid data. By for table editing we don't want
         * trailing bad data to be considered a "valid value". So set the index
         * to 0 so that the parse(Object) method knows that we had an error.
         */
        /*
        if (pos.getIndex() != source.length()) {
            pos.setErrorIndex(pos.getIndex());
            pos.setIndex(0);
        }*/
        return val;
    }

    @Override
    public StringBuffer format(double number, StringBuffer toAppendTo,
            FieldPosition pos) {
        return childFormat.format(number, toAppendTo, pos);
    }

    @Override
    public StringBuffer format(long number, StringBuffer toAppendTo,
            FieldPosition pos) {
        return childFormat.format(number, toAppendTo, pos);
    }

}
