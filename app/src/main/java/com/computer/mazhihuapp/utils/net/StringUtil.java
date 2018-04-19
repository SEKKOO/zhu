package com.computer.mazhihuapp.utils.net;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {


	public static boolean isBlank(String str) {
		boolean b = false;
		if (str == null || "".equals(str) || "null".equals(str)
				|| "NULL".equals(str)) {
			b = true;
		} else {
			b = false;
		}
		return b;
	}


	public static boolean isEmail(String email) {
		String str = "^([a-zA-Z0-9]*[-_.]?[a-zA-Z0-9]+)*@([a-zA-Z0-9]*[-_]?[a-zA-Z0-9]+)+[\\.][A-Za-z]{2,3}([\\.][A-Za-z]{2})?$";
		Pattern p = Pattern.compile(str);
		Matcher m = p.matcher(email);
		return m.matches();
	}




	/**
	 * ��ȡ���е�ǰ10Ϊ��Ϊ����
	 * 
	 * @param str
	 * @return
	 */
	public static String getNearBase(String str, int length) {
		String re = null;
		if (length > 7)
			length = 7;
		if (str.length() > 10) {
			re = str.substring(0, length);
		}
		return re;

	}
	/**
	 * �滻�ַ�
	 * 
	 * @param text
	 * @param value
	 *            Ҫ�滻�ɵ��ַ�
	 * @return
	 */
	public static String replace(String text, String value) {
		String[] lines = text.split("&&P&&");
		Pattern pattern = Pattern.compile("\\*\\d+\\*");

		Map<String, String> maps = new HashMap<String, String>();
		for (String line : lines) {
			Matcher matcher = pattern.matcher(line);
			while (matcher.find()) {
				if (!maps.containsKey(matcher.group())) {
					String key = matcher.group();
					int len = Integer.parseInt(key.replace("*", ""));
					StringBuffer sb = new StringBuffer();
					for (int i = 0; i < len; i++) {
						sb.append(value);
					}
					maps.put(key, sb.toString());
				}
			}
		}
		for (String key : maps.keySet()) {
			text = text.replace(key, maps.get(key));
		}
		return text;
	}

	/**
	 * ������ַ���
	 * 
	 * @param str
	 * @return String
	 */
	public static String doEmpty(String str) {
		return doEmpty(str, "");
	}

	/**
	 * ������ַ���
	 * 
	 * @param str
	 * @param defaultValue
	 * @return String
	 */
	public static String doEmpty(String str, String defaultValue) {
		if (str == null || str.equalsIgnoreCase("null")
				|| str.trim().equals("") || str.trim().equals("����ѡ��")) {
			str = defaultValue;
		} else if (str.startsWith("null")) {
			str = str.substring(4, str.length());
		}
		return str.trim();
	}

}
