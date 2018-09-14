package iuliiaponomareva.evroscudo.parsers;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import iuliiaponomareva.evroscudo.Currency;


public abstract class ExchangeRatesXMLParser extends ExchangeRatesParser {

    abstract String getURL();

    protected static String readTag(XmlPullParser parser, String tag) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, tag);
        String result = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, tag);
        return result;
    }

    private static String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }


    abstract List<Currency> parseData(XmlPullParser parser) throws IOException, XmlPullParserException;

    @Override
    public List<Currency> parse() {
        InputStream inputStream = null;
        try {
            inputStream = downloadUrl(getURL());
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(inputStream, null);
            return parseData(parser);
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return new ArrayList<>();
    }



    protected static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }

    }
}
