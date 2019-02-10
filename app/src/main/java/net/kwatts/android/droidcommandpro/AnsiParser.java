package net.kwatts.android.droidcommandpro;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;

public class AnsiParser {

    private int defaultTextColor = Color.GREEN;
    private int defaultBackgroundColor = Color.BLACK;

    private ForegroundColorSpan mForegroundColorSpan = new ForegroundColorSpan(defaultTextColor);
    // do nothing right now with bg
    private BackgroundColorSpan mBackgroundColorSpan = new BackgroundColorSpan(defaultBackgroundColor);
    private int mIntensity = 0;



    /** The result of searching for ANSI escape sequences in a given text input.*/
    private class AnsiMatcher {
        private static final int STATE_CSI1		= 1;
        private static final int STATE_CSI2 	= 2;
        private static final int STATE_PARAM	= 3;
        private static final int STATE_COMMAND	= 6;
        private static final int STATE_RESET	= 7;

        private int mState			= STATE_CSI1;

        private String mText		= "";
        private int	mPos			= 0;
        private int mStart			= 0;
        private int mEnd			= 0;

        private char mCommand		= ' ';
        private String mParam		= "";

        private static final int PARAM_COUNT	= 5;
        private int[] mParams		= new int[PARAM_COUNT];
        private int mParamIndex		= -1;

        /** Returns the input text. */
        public String text() 		{ return mText; }
        /** Returns the index of the first character of the ANSI escape sequence. */
        public int start() 			{ return mStart; }
        /** Returns the index of the first character following the ANSI escape sequence. */
        public int end() 			{ return mEnd; }
        /** Returns the ANSI command identifier. */
        public char command() 		{ return mCommand; }
        /** Returns a given parameter. */
        public int param(int i) 	{ return mParams[i]; }
        /** Returns the amount of parameters in the ANSI escape sequence. */
        public int paramCount() 	{ return mParamIndex+1; }

        /** Sets the text to be used as input.
         * @param text The text to be used as input.
         */
        public void match(String text) {
            mText	= text;
            mPos 	= 0;
            mStart	= 0;
            mEnd	= 0;
        }

        /** Returns the next occurrence of an ANSI escape sequence in the input.
         * @return true if an escape sequence has been found.
         */
        public boolean find() {
            while (mPos < mText.length()) {
                switch (mState) {
                    case STATE_CSI1:
                        mPos = mText.indexOf(0x001B, mPos);
                        if (mPos != -1) {
                            mState = STATE_CSI2;  mStart = mPos;  mPos++;
                        } else { mPos = mText.length(); }
                        break;
                    case STATE_CSI2:
                        if (mText.charAt(mPos) == '[') {
                            mState = STATE_PARAM;  mPos++;
                        } else { mState = STATE_RESET; }
                        break;
                    case STATE_PARAM:
                        if ((mText.charAt(mPos) >= '0') && (mText.charAt(mPos) <= '9')) {
                            mParam = mParam + mText.charAt(mPos);
                            mPos++;
                        } else {
                            mParamIndex++;
                            if (mParamIndex < PARAM_COUNT) {
                                try {
                                    mParams[mParamIndex] = Integer.parseInt(mParam);
                                } catch (NumberFormatException e) {
                                    mParams[mParamIndex] = -1;
                                }
                            }
                            mParam = "";

                            if (mText.charAt(mPos) == ';') {
                                mPos++;
                            } else { mState = STATE_COMMAND; }
                        }
                        break;
                    case STATE_COMMAND:
                        mCommand = mText.charAt(mPos);
                        mPos++; mEnd = mPos;
                        mState = STATE_RESET;
                        return true;
                    case STATE_RESET:
                        mParam = "";  mParamIndex = -1;
                        mState = STATE_CSI1;
                        break;
                }
            }
            return false;
        }

        /** Returns whether a partial ANSI escape sequence has been detected.
         * @return true if a partial escape sequence has been found.
         */
        public boolean foundPartial() {
            return (mState != STATE_CSI1);
        }

    }
    private AnsiMatcher mMatcher = new AnsiMatcher();

    /** Scans the provided text and turns all occurrences of ANSI color codes into ColorSpans.
     * @param sequence The text to be scanned for color codes.
     * @return Spannable containing the input text with added color codes.
     */
    public Spannable parse(CharSequence sequence) {
        SpannableStringBuilder builder = new SpannableStringBuilder();


        if (mMatcher.foundPartial()) {
            String partial = new String(mMatcher.text().substring(mMatcher.start()));
            sequence = partial + sequence;
        }

        String text = sequence.toString();
        mMatcher.match(text);
        int pos = 0;
        while (mMatcher.find()) {
            if (mMatcher.start() > pos) {
                Spannable prefix = new SpannableString(text.substring(pos, mMatcher.start()));
                prefix.setSpan(mForegroundColorSpan, 0, prefix.length(), 0);
                // do nothing right now with bg
                //prefix.setSpan(mBackgroundColorSpan, 0, prefix.length(), 0);
                builder.append(prefix);
            }
            char command = mMatcher.command();
            if (command == 'm') {
                if (mMatcher.paramCount() == 0) {
                    mIntensity = 0;
                } else {
                    for (int i = 0; i < mMatcher.paramCount(); i++) {
                        int param = mMatcher.param(i);
                        // 0 is reset/normal, 1 bold or increased intensity
                        if ((param == 0) || (param == 1)) {
                            mIntensity = param;
                        }
                        else if ((param > 29) && (param < 38)) { //foreground colors
                            int c = getColor(param);
                            mForegroundColorSpan = new ForegroundColorSpan(c);
                        }
                        else if ((param > 39) && (param < 48)) { //background colors, don't change
                            mForegroundColorSpan = new ForegroundColorSpan(defaultTextColor);
                           // mBackgroundColorSpan = new BackgroundColorSpan(getColor(param));
                        }
                    }
                }
            }
            pos = mMatcher.end();
        }
        if (mMatcher.foundPartial() == false) {
            if (pos < text.length()) {
                Spannable postfix = new SpannableString(text.substring(pos));
                postfix.setSpan(mForegroundColorSpan, 0, postfix.length(), 0);
                //postfix.setSpan(mBackgroundColorSpan, 0, postfix.length(), 0);
                builder.append(postfix);
            }
        }
        return builder;
    }

    private int getColor(int parameter) {
        // http://en.wikipedia.org/wiki/ANSI_escape_code - xterm colors.
        switch (parameter % 10) {
            case 0: return (mIntensity == 0) ? defaultTextColor : defaultTextColor;
            case 1: return (mIntensity == 0) ? defaultTextColor : defaultTextColor;
            case 2: return (mIntensity == 0) ? 0xff00cc00 : 0xff00ff00;
            case 3: return (mIntensity == 0) ? 0xffcccc00 : 0xffffff00;
            case 4: return (mIntensity == 0) ? 0xff0000ee : 0xff5555ff; // blue is odd.
            case 5: return (mIntensity == 0) ? 0xffcc00cc : 0xffff00ff;
            case 6: return (mIntensity == 0) ? 0xff00cccc : 0xff00ffff;
            case 7: return (mIntensity == 0) ? 0xffcccccc : 0xffffffff;
        }
        return Color.CYAN;


    }



}