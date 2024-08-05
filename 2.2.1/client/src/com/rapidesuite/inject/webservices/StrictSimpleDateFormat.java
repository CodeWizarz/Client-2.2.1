package com.rapidesuite.inject.webservices;

import java.text.DateFormatSymbols;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Extension of SimpleDateFormat that implements strict matching.
 * parse(text) will only return a Date if text exactly matches the
 * pattern. 
 * 
 * This is needed because SimpleDateFormat does not enforce strict 
 * matching. First there is the lenient setting, which is true
 * by default. This allows text that does not match the pattern and
 * garbage to be interpreted as valid date/time information. For example,
 * parsing "2010-09-01" using the format "yyyyMMdd" yields the date 
 * 2009/12/09! Is this bizarre interpretation the ninth day of the  
 * zeroth month of 2010? If you are dealing with inputs that are not 
 * strictly formatted, you WILL get bad results. You can override lenient  
 * with setLenient(false), but this strangeness should not be the default. 
 *
 * Second, setLenient(false) still does not strictly interpret the pattern. 
 * For example "2010/01/5" will match "yyyy/MM/dd". And data disagreement like 
 * "1999/2011" for the pattern "yyyy/yyyy" is tolerated (yielding 2011). 
 *
 * Third, setLenient(false) still allows garbage after the pattern match. 
 * For example: "20100901" and "20100901andGarbage" will both match "yyyyMMdd". 
 * 
 * This class restricts this undesirable behavior, and makes parse() and 
 * format() functional inverses, which is what you would expect. Thus
 * text.equals(format(parse(text))) when parse returns a non-null result.
 * 
 * @author zobell
 *
 */
@SuppressWarnings("serial")
public class StrictSimpleDateFormat extends SimpleDateFormat {

    protected boolean strict = true;

    public StrictSimpleDateFormat() {
        super();
        setStrict(true);
    }

    public StrictSimpleDateFormat(String pattern) {
        super(pattern);
        setStrict(true);
    }

    public StrictSimpleDateFormat(String pattern, DateFormatSymbols formatSymbols) {
        super(pattern, formatSymbols);
        setStrict(true);
    }

    public StrictSimpleDateFormat(String pattern, Locale locale) {
        super(pattern, locale);
        setStrict(true);
    }

    /**
     * Set the strict setting. If strict == true (the default)
     * then parsing requires an exact match to the pattern. Setting
     * strict = false will tolerate text after the pattern match. 
     * @param strict
     */
    public void setStrict(boolean strict) {
        this.strict = strict;
        // strict with lenient does not make sense. Really lenient does
        // not make sense in any case.
        if (strict)
            setLenient(false); 
    }

    public boolean getStrict() {
        return strict;
    }

    /**
     * Parse text to a Date. Exact match of the pattern is required.
     * Parse and format are now inverse functions, so this is
     * required to be true for valid text date information:
     * text.equals(format(parse(text))
     * @param text
     * @param pos
     * @return
     */
    @Override
    public Date parse(String text, ParsePosition pos) {
        int posIndex = pos.getIndex();
        Date d = super.parse(text, pos);
        if (strict && d != null) {
           String format = this.format(d);
           if (posIndex + format.length() != text.length() ||
                 !text.endsWith(format)) {
              d = null; // Not exact match
           }
        }
        return d;
    }
    
}