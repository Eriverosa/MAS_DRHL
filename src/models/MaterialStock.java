package src.models;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import src.commons.ParametersConfig;

// Stock INDEPENDIENTE por agente. No depende del universo global de tamanos.
public class MaterialStock {

    ArrayList<Stock> materialStock = new ArrayList<>();

    // Constructor principal: recibe stock emparejado por tamano (orden irrelevante).
    public MaterialStock(Map<Integer, Integer> stockBySize) {
        for (Map.Entry<Integer, Integer> entry : stockBySize.entrySet()) {
            this.materialStock.add(new Stock(entry.getKey(), entry.getValue()));
        }
        ordenar();
    }

    // Constructor vacio: acumulador, sin precargar tamanos globales.
    public MaterialStock() {
    }

    // Crea un MaterialStock desde un JSON tipo {"500":10,"1000":20}
    public static MaterialStock fromJson(String json) {
        Type type = new TypeToken<LinkedHashMap<String, Integer>>() {}.getType();
        LinkedHashMap<String, Integer> raw = new Gson().fromJson(json, type);
        Map<Integer, Integer> stock = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : raw.entrySet()) {
            stock.put(Integer.parseInt(e.getKey()), e.getValue());
        }
        return new MaterialStock(stock);
    }

    // Ordena por tamano ascendente (solo estetica/prolijidad).
    private void ordenar() {
        this.materialStock.sort(Comparator.comparing(Stock::getTamanho));
    }

    public Integer getTotalAmountHelpByPerson() {
        Integer totalQuantity = 0;
        for (Stock stock : this.getMaterialStock()) {
            totalQuantity += ((stock.getTamanho() * stock.getCantidad()) / ParametersConfig.AMOUNT_BY_PERSON_CC);
        }
        return totalQuantity;
    }

    public Integer getTotalAmountHelpByCC() {
        Integer totalQuantity = 0;
        for (Stock stock : this.getMaterialStock()) {
            totalQuantity += (stock.getTamanho() * stock.getCantidad());
        }
        return totalQuantity;
    }

    public Boolean getNeedHelp(int amountPeople) {
        return this.getTotalAmountHelpByPerson() <= ParametersConfig.DISTRIBUTION_CANTIDAD_MINIMA_STOCK_PERCENT
                * amountPeople;
    }

    public int getTotalNeedHelp(int amountPeople) {
        return amountPeople - this.getTotalAmountHelpByPerson();
    }

    public void agregarMaterial(Stock material) {
        for (Stock existingMaterial : materialStock) {
            if (existingMaterial.getTamanho().equals(material.getTamanho())) {
                existingMaterial.setCantidad(existingMaterial.getCantidad() + 1);
                return;
            }
        }
        materialStock.add(material);
        ordenar();
    }

    public void eliminarMaterial(Stock material) {
        materialStock.remove(material);
    }

    public ArrayList<Stock> getMaterialStock() {
        return materialStock;
    }

    public void setMaterialStock(ArrayList<Stock> materialStock) {
        this.materialStock = materialStock;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    @Override
    public MaterialStock clone() {
        MaterialStock clonedStock = new MaterialStock();
        for (Stock stock : this.materialStock) {
            clonedStock.materialStock.add(new Stock(stock.getTamanho(), stock.getCantidad()));
        }
        return clonedStock;
    }

    public MaterialStock getOptimeCombination(int cantidad) {
        MaterialStock tempMaterialStock = this.clone();
        MaterialStock resultMaterialStock = new MaterialStock();
        for (int i = 0; i < cantidad; i++) {
            Boolean resp = dynamicProgrammingAlgorithm(tempMaterialStock, resultMaterialStock,
                    ParametersConfig.AMOUNT_BY_PERSON_CC);
            if (!resp)
                break;
        }
        return resultMaterialStock;
    }

    public boolean dynamicProgrammingAlgorithm(MaterialStock originalStock, MaterialStock resultingStock,
            int targetValue) {
        List<Stock> sortedStock = new ArrayList<>(originalStock.materialStock);
        sortedStock.sort(Comparator.comparing(stock -> stock.tamanho));

        int n = sortedStock.size();
        int[][] dp = new int[n + 1][targetValue + 1];
        int INF = 1000000000;
        for (int i = 0; i <= n; i++) {
            for (int j = 0; j <= targetValue; j++) {
                dp[i][j] = INF;
            }
        }
        dp[0][0] = 0;

        for (int i = 1; i <= n; i++) {
            for (int j = 0; j <= targetValue; j++) {
                dp[i][j] = dp[i - 1][j];
                Stock stock = sortedStock.get(i - 1);
                for (int k = 1; k <= stock.cantidad; k++) {
                    if (k * stock.tamanho <= j) {
                        dp[i][j] = Math.min(dp[i][j], dp[i - 1][j - k * stock.tamanho] + k);
                    }
                }
            }
        }

        if (dp[n][targetValue] != INF) {
            ArrayList<Stock> solution = new ArrayList<>();
            int i = n;
            int j = targetValue;
            while (i > 0 && j > 0) {
                if (dp[i][j] == dp[i - 1][j]) {
                    i--;
                } else {
                    Stock stock = sortedStock.get(i - 1);
                    int k;
                    for (k = 1; k <= stock.cantidad; k++) {
                        if (k * stock.tamanho <= j && dp[i][j] == dp[i - 1][j - k * stock.tamanho] + k) {
                            break;
                        }
                    }
                    solution.add(new Stock(stock.tamanho, k));
                    j -= k * stock.tamanho;
                    i--;
                }
            }

            for (Stock usedStock : solution) {
                for (Stock originalStockItem : originalStock.materialStock) {
                    if (usedStock.tamanho == originalStockItem.tamanho) {
                        originalStockItem.cantidad -= usedStock.cantidad;
                        break;
                    }
                }
            }

            for (Stock usedStock : solution) {
                boolean found = false;
                for (Stock resultingStockItem : resultingStock.materialStock) {
                    if (usedStock.tamanho == resultingStockItem.tamanho) {
                        resultingStockItem.cantidad += usedStock.cantidad;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    resultingStock.materialStock.add(new Stock(usedStock.tamanho, usedStock.cantidad));
                    resultingStock.ordenar();
                }
            }
            return true;
        }
        return false;
    }

    // Si llega un tamano nuevo, se AGREGA (antes reventaba). Ordena tras insertar.
    public void addMaterialStock(MaterialStock addStock) {
        for (Stock stock : addStock.getMaterialStock().stream().filter(e -> e.getCantidad() > 0)
                .collect(Collectors.toList())) {
            Stock stockEncontrado = this.getMaterialStock().stream()
                    .filter(e -> e.getTamanho().equals(stock.getTamanho()))
                    .findFirst().orElse(null);
            if (stockEncontrado == null) {
                this.materialStock.add(new Stock(stock.getTamanho(), stock.getCantidad()));
                ordenar();
            } else {
                stockEncontrado.setCantidad(stockEncontrado.getCantidad() + stock.getCantidad());
            }
        }
    }

    // Descuenta hasta 0 pero NUNCA elimina la key (historial). No revienta si falta.
    public void removeMaterialStock(MaterialStock removeStock) {
        for (Stock stock : removeStock.getMaterialStock().stream().filter(e -> e.getCantidad() > 0)
                .collect(Collectors.toList())) {
            Stock stockEncontrado = this.getMaterialStock().stream()
                    .filter(e -> e.getTamanho().equals(stock.getTamanho()))
                    .findFirst().orElse(null);
            if (stockEncontrado != null) {
                stockEncontrado.setCantidad(Math.max(stockEncontrado.getCantidad() - stock.getCantidad(), 0));
            }
        }
    }

    public void discountMaterialStockByTime() {
        int resultadoRedondeado = (int) Math
                .round((double) ParametersConfig.EXECUTION_ADD_TIME / ParametersConfig.DELAY_BY_PERSON_TIME);
        MaterialStock materialStock = this.getOptimeCombination(resultadoRedondeado);
        this.removeMaterialStock(materialStock);
    }
}