package io.github.yangziwen.zyftp.command.impl.listing;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

import io.github.yangziwen.zyftp.filesystem.FileView;

public class LISTFileFormatter implements FileFormatter {

    private final static char DELIM = ' ';

    private final static char[] NEWLINE = { '\r', '\n' };

	@Override
	public String format(FileView file) {
        StringBuilder sb = new StringBuilder();
		        sb.append(getPermission(file));
		        sb.append(DELIM);
		        sb.append(DELIM);
		        sb.append(DELIM);
		        sb.append(String.valueOf(file.getLinkCount()));
		        sb.append(DELIM);
		        sb.append(file.getOwnerName());
		        sb.append(DELIM);
		        sb.append(file.getGroupName());
		        sb.append(DELIM);
		        sb.append(getLength(file));
		        sb.append(DELIM);
		        sb.append(getLastModified(file));
		        sb.append(DELIM);
		        sb.append(file.getName());
		        sb.append(NEWLINE);
        return sb.toString();
	}


    /**
     * Get size
     */
    private String getLength(FileView file) {
        String initStr = "            ";
        long sz = 0;
        if (file.isFile()) {
            sz = file.getSize();
        }
        String szStr = String.valueOf(sz);
        if (szStr.length() > initStr.length()) {
            return szStr;
        }
        return initStr.substring(0, initStr.length() - szStr.length()) + szStr;
    }

    /**
     * Get last modified date string.
     */
    private String getLastModified(FileView file) {
        return getUnixDate(file.getLastModified());
    }

    /**
     * Get permission string.
     */
    private char[] getPermission(FileView file) {
        char permission[] = new char[10];
        Arrays.fill(permission, '-');

        permission[0] = file.isDirectory() ? 'd' : '-';
        permission[1] = file.isReadable() ? 'r' : '-';
        permission[2] = file.isWritable() ? 'w' : '-';
        permission[3] = file.isDirectory() ? 'x' : '-';
        return permission;
    }

    /**
     * Get unix style date string.
     */
    public final static String getUnixDate(long millis) {
       String[] months = { "Jan", "Feb", "Mar", "Apr", "May",
                "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
        if (millis < 0) {
            return "------------";
        }

        StringBuilder sb = new StringBuilder(16);
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(millis);

        // month
        sb.append(months[cal.get(Calendar.MONTH)]);
        sb.append(' ');

        // day
        int day = cal.get(Calendar.DATE);
        if (day < 10) {
            sb.append(' ');
        }
        sb.append(day);
        sb.append(' ');

        long sixMonth = 15811200000L; // 183L * 24L * 60L * 60L * 1000L;
        long nowTime = System.currentTimeMillis();
        if (Math.abs(nowTime - millis) > sixMonth) {

            // year
            int year = cal.get(Calendar.YEAR);
            sb.append(' ');
            sb.append(year);
        } else {

            // hour
            int hh = cal.get(Calendar.HOUR_OF_DAY);
            if (hh < 10) {
                sb.append('0');
            }
            sb.append(hh);
            sb.append(':');

            // minute
            int mm = cal.get(Calendar.MINUTE);
            if (mm < 10) {
                sb.append('0');
            }
            sb.append(mm);
        }
        return sb.toString();
    }

}
