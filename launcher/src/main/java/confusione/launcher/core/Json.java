package confusione.launcher.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Minimal JSON reader (objects -> Map, arrays -> List, numbers -> Double). Enough for Mojang metadata. */
public final class Json {

	private final String s;
	private int i;

	private Json(String s) {
		this.s = s;
	}

	public static Object parse(String text) {
		Json p = new Json(text);
		p.ws();
		Object v = p.value();
		p.ws();
		if (p.i != p.s.length()) {
			throw p.err("trailing data");
		}
		return v;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> obj(Object o) {
		return (Map<String, Object>) o;
	}

	@SuppressWarnings("unchecked")
	public static List<Object> arr(Object o) {
		return o == null ? new ArrayList<Object>() : (List<Object>) o;
	}

	public static String str(Map<String, Object> m, String key) {
		Object o = m.get(key);
		return o == null ? null : o.toString();
	}

	public static long num(Map<String, Object> m, String key) {
		Object o = m.get(key);
		return o instanceof Double ? ((Double) o).longValue() : -1;
	}

	private Object value() {
		if (i >= s.length()) {
			throw err("unexpected end");
		}
		char c = s.charAt(i);
		switch (c) {
		case '{': return object();
		case '[': return array();
		case '"': return string();
		case 't': expect("true"); return Boolean.TRUE;
		case 'f': expect("false"); return Boolean.FALSE;
		case 'n': expect("null"); return null;
		default: return number();
		}
	}

	private Map<String, Object> object() {
		Map<String, Object> m = new LinkedHashMap<String, Object>();
		i++;
		ws();
		if (peek() == '}') {
			i++;
			return m;
		}
		while (true) {
			ws();
			String k = string();
			ws();
			if (s.charAt(i++) != ':') {
				throw err("expected ':'");
			}
			ws();
			m.put(k, value());
			ws();
			char c = s.charAt(i++);
			if (c == '}') {
				return m;
			}
			if (c != ',') {
				throw err("expected ',' or '}'");
			}
		}
	}

	private List<Object> array() {
		List<Object> l = new ArrayList<Object>();
		i++;
		ws();
		if (peek() == ']') {
			i++;
			return l;
		}
		while (true) {
			ws();
			l.add(value());
			ws();
			char c = s.charAt(i++);
			if (c == ']') {
				return l;
			}
			if (c != ',') {
				throw err("expected ',' or ']'");
			}
		}
	}

	private String string() {
		if (s.charAt(i) != '"') {
			throw err("expected string");
		}
		i++;
		StringBuilder b = new StringBuilder();
		while (true) {
			char c = s.charAt(i++);
			if (c == '"') {
				return b.toString();
			}
			if (c == '\\') {
				char e = s.charAt(i++);
				switch (e) {
				case 'n': b.append('\n'); break;
				case 't': b.append('\t'); break;
				case 'r': b.append('\r'); break;
				case 'b': b.append('\b'); break;
				case 'f': b.append('\f'); break;
				case 'u': b.append((char) Integer.parseInt(s.substring(i, i + 4), 16)); i += 4; break;
				default: b.append(e);
				}
			} else {
				b.append(c);
			}
		}
	}

	private Double number() {
		int start = i;
		while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) {
			i++;
		}
		if (start == i) {
			throw err("unexpected character");
		}
		return Double.valueOf(s.substring(start, i));
	}

	private void expect(String word) {
		if (!s.startsWith(word, i)) {
			throw err("expected " + word);
		}
		i += word.length();
	}

	private char peek() {
		return i < s.length() ? s.charAt(i) : 0;
	}

	private void ws() {
		while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
			i++;
		}
	}

	private IllegalArgumentException err(String msg) {
		return new IllegalArgumentException("JSON: " + msg + " at " + i);
	}
}
