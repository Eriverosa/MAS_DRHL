package src.commons;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Extrae el tamano (en cc) desde el nombre de una columna de stock, usando el
// patron de config.json (materialStockSizesHeaderCSV), ej "StockBotellas_{size}_cc".
public class StockHeaderParser {
    private static final Pattern PATTERN = buildPattern(ParametersConfig.MATERIAL_STOCK_SIZES_HEADER);

    private static Pattern buildPattern(String template) {
        int idx = template.indexOf("{size}");
        String prefix = template.substring(0, idx);
        String suffix = template.substring(idx + "{size}".length());
        return Pattern.compile(Pattern.quote(prefix) + "(\\d+)" + Pattern.quote(suffix));
    }

    public static Integer parseSize(String header) {
        Matcher m = PATTERN.matcher(header.trim());
        if (m.matches()) {
            return Integer.parseInt(m.group(1));
        }
        return null;
    }
}