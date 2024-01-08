package src.models;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import src.commons.ParametersConfig;

public class MaterialStock {
    // String unitOfMeasurement = "cc";
    // ArrayList<Integer> materialStockSizes = new ArrayList<>(Arrays.asList(20000,
    // 2250, 2000, 1600, 1000, 500));
    // @SerializedName("materialStock")
    ArrayList<Stock> materialStock = new ArrayList<>();

    public MaterialStock(ArrayList<Integer> listValues) {
        int index = 0;
        for (Integer integer : ParametersConfig.MATERIAL_STOCK_SIZES) {
            this.getMaterialStock().add(new Stock(integer, listValues.get(index)));
            index++;
        }
    }

    public MaterialStock() {
        for (Integer integer : ParametersConfig.MATERIAL_STOCK_SIZES) {
            this.getMaterialStock().add(new Stock(integer, 0));
        }
    }

    public MaterialStock(Boolean val) {
        this.getMaterialStock().add(new Stock(500, new Random().nextInt(100)));
        this.getMaterialStock().add(new Stock(1000, new Random().nextInt(100)));
        this.getMaterialStock().add(new Stock(1600, new Random().nextInt(100)));
        this.getMaterialStock().add(new Stock(2000, new Random().nextInt(100)));
        this.getMaterialStock().add(new Stock(2250, new Random().nextInt(100)));
        this.getMaterialStock().add(new Stock(20000, new Random().nextInt(100)));
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
        return this.getTotalAmountHelpByPerson() <= ParametersConfig.CANTIDAD_MINIMA_STOCK_PERCENT * amountPeople;
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
        // Si el material no existe en la combinación actual, agrégalo
        materialStock.add(material);
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
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    // public int getCantidadTotal() {
    // int totalMateriales = 0;
    // for (Stock material : materialStock) {
    // totalMateriales += material.getCantidad();
    // }
    // return totalMateriales;
    // }

    @Override
    public MaterialStock clone() {
        MaterialStock clonedStock = new MaterialStock();
        for (Stock stock : clonedStock.getMaterialStock()) {
            Stock searchStock = this.getMaterialStock().stream()
                    .filter(stockVal -> stockVal.getTamanho().equals(stock.getTamanho()))
                    .findFirst()
                    .orElse(null);
            if (searchStock != null) {
                stock.setCantidad(searchStock.getCantidad());
            }
        }
        return clonedStock;
    }

    public MaterialStock getOptimeCombination(int cantidad) {
        MaterialStock tempMaterialStock = this.clone();
        // MaterialStock discountedMaterialStock = new MaterialStock();
        MaterialStock resultMaterialStock = new MaterialStock();

        Integer nHelps = 0;
        for (int i = 0; i < cantidad; i++) {
            Boolean resp = dynamicProgrammingAlgorithm(tempMaterialStock, resultMaterialStock,
                    ParametersConfig.AMOUNT_BY_PERSON_CC);
            if (!resp)
                break;
            nHelps++;
        }
        return resultMaterialStock;
    }

    public boolean dynamicProgrammingAlgorithm(MaterialStock originalStock, MaterialStock resultingStock,
            int targetValue) {
        List<Stock> sortedStock = new ArrayList<>(originalStock.materialStock);
        sortedStock.sort(Comparator.comparing(stock -> stock.tamanho));

        int n = sortedStock.size();
        int[][] dp = new int[n + 1][targetValue + 1];

        // Inicializa la matriz dp con valores grandes para representar "infinito"
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
            // Reconstruye la solución
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

            // Ajusta el stock original
            for (Stock usedStock : solution) {
                for (Stock originalStockItem : originalStock.materialStock) {
                    if (usedStock.tamanho == originalStockItem.tamanho) {
                        originalStockItem.cantidad -= usedStock.cantidad;
                        break;
                    }
                }
            }

            // Suma la solución al stock resultante
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
                }
            }

            return true; // Se encontró una solución
        }

        return false; // No se encontró una solución
    }

    public void addMaterialStock(MaterialStock addStock) {
        for (Stock stock : addStock.getMaterialStock().stream().filter(elemento -> elemento.getCantidad() > 0)
                .collect(Collectors.toList())) {
            Stock stockEncontrado = this.getMaterialStock().stream()
                    .filter(elemento -> elemento.getTamanho().equals(stock.getTamanho()))
                    .findFirst()
                    .orElse(null);
            int newStock = stockEncontrado.getCantidad() + stock.getCantidad();
            stockEncontrado.setCantidad(newStock);
        }
    }

    public void removeMaterialStock(MaterialStock addStock) {
        for (Stock stock : addStock.getMaterialStock().stream().filter(elemento -> elemento.getCantidad() > 0)
                .collect(Collectors.toList())) {
            Stock stockEncontrado = this.getMaterialStock().stream()
                    .filter(elemento -> elemento.getTamanho().equals(stock.getTamanho()))
                    .findFirst()
                    .orElse(null);
            int newStock = stockEncontrado.getCantidad() - stock.getCantidad();
            stockEncontrado.setCantidad(newStock);
        }
    }

    public void discountMaterialStockByTime() {
        int resultadoRedondeado = (int) Math
                .round((double) ParametersConfig.EXECUTION_ADD_TIME / ParametersConfig.DELAY_BY_PERSON_TIME);
        MaterialStock materialStock = this.getOptimeCombination(resultadoRedondeado);
        this.removeMaterialStock(materialStock);
    }

}
