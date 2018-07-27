package io.github.yangziwen.zyftp.command.impl.listing;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import io.github.yangziwen.zyftp.filesystem.FileView;

public class MLSTFileFormatter implements FileFormatter {

    public static final String[] DEFAULT_TYPES = new String[] { "Size", "Modify", "Type" };

    private final static char[] NEWLINE = { '\r', '\n' };

    private String[] selectedTypes = DEFAULT_TYPES;

    public MLSTFileFormatter(String[] selectedTypes) {
        if (selectedTypes != null) {
            this.selectedTypes = selectedTypes.clone();
        }
    }

	/**
     * @see FileFormater#format(FtpFile)
     */
    @Override
	public String format(FileView file) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < selectedTypes.length; ++i) {
            String type = selectedTypes[i];
            if (type.equalsIgnoreCase("size")) {
                sb.append("Size=");
                sb.append(String.valueOf(file.getSize()));
                sb.append(';');
            } else if (type.equalsIgnoreCase("modify")) {
                String timeStr = getFtpDate(file.getLastModified());
                sb.append("Modify=");
                sb.append(timeStr);
                sb.append(';');
            } else if (type.equalsIgnoreCase("type")) {
                if (file.isFile()) {
                    sb.append("Type=file;");
                } else if (file.isDirectory()) {
                    sb.append("Type=dir;");
                }
            } else if (type.equalsIgnoreCase("perm")) {
                sb.append("Perm=");
                if (file.isReadable()) {
                    if (file.isFile()) {
                        sb.append('r');
                    } else if (file.isDirectory()) {
                        sb.append('e');
                        sb.append('l');
                    }
                }
                if (file.isWritable()) {
                    if (file.isFile()) {
                        sb.append('a');
                        sb.append('d');
                        sb.append('f');
                        sb.append('w');
                    } else if (file.isDirectory()) {
                        sb.append('f');
                        sb.append('p');
                        sb.append('c');
                        sb.append('m');
                    }
                }
                sb.append(';');
            }
        }
        sb.append(' ');
        sb.append(file.getName());

        sb.append(NEWLINE);

        return sb.toString();
    }

    private static final TimeZone TIME_ZONE_UTC = TimeZone.getTimeZone("UTC");

    public final static String getFtpDate(long millis) {
        StringBuilder sb = new StringBuilder(20);

        // MLST should use UTC
        Calendar cal = new GregorianCalendar(TIME_ZONE_UTC);
        cal.setTimeInMillis(millis);


        // year
        sb.append(cal.get(Calendar.YEAR));

        // month
        int month = cal.get(Calendar.MONTH) + 1;
        if (month < 10) {
            sb.append('0');
        }
        sb.append(month);

        // date
        int date = cal.get(Calendar.DATE);
        if (date < 10) {
            sb.append('0');
        }
        sb.append(date);

        // hour
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if (hour < 10) {
            sb.append('0');
        }
        sb.append(hour);

        // minute
        int min = cal.get(Calendar.MINUTE);
        if (min < 10) {
            sb.append('0');
        }
        sb.append(min);

        // second
        int sec = cal.get(Calendar.SECOND);
        if (sec < 10) {
            sb.append('0');
        }
        sb.append(sec);

        // millisecond
        sb.append('.');
        int milli = cal.get(Calendar.MILLISECOND);
        if (milli < 100) {
            sb.append('0');
        }
        if (milli < 10) {
            sb.append('0');
        }
        sb.append(milli);
        return sb.toString();
    }

}
