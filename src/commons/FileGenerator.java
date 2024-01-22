package src.commons;

import com.opencsv.CSVWriterBuilder;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import com.opencsv.ICSVWriter;

import src.models.SupplyActivity;
import src.models.SupplyActivityOrder;

public class FileGenerator {
    String pathFolder = "./src/results/";

    // String csvFilePath = "ruta/del/archivo.csv";
    public void generateFile(String nameDocument, List<SupplyActivity> activitieSupplyActivityList) {
        String filePath = pathFolder.concat(nameDocument).concat("_results.csv");
        char delimiter = ';';
        char quotechar = '\0';

        try (ICSVWriter writer = new CSVWriterBuilder(new FileWriter(filePath))
                .withSeparator(delimiter)
                .withQuoteChar(quotechar)
                .build()) {
            String[] header = { "horaInicioViajeCarga", "tiempoViajeCarga", "horaLlegadaViajeCarga", "horaInicioCarga",
                    "tiempoCarga", "horaFinCarga", "horaInicioViajeDescarga", "tiempoViajeDescarga",
                    "horaLlegadaViajeDescarga", "horaInicioDescarga", "tiempoDescarga", "horaFinDescarga",
                    "pointNameSupplyActivityProposed", "pointNameSupplyActivityRequired",
                    "pointNameSupplyActivityTransportation", "ayudaRequerida", "ayudaTransportada",
                    "tiempoDuraciónConversacion", "cantidadNegociacionesTotal" };
            writer.writeNext(header);
            for (SupplyActivity supplyActivity : activitieSupplyActivityList) {
                SupplyActivityOrder supplyActivityOrder = supplyActivity.getSupplyActivityOrder();
                String[] row = {
                        String.valueOf((double) CustomUnits.pipeStandarTime(
                                supplyActivityOrder.getHoraInicioViajeCarga(),
                                CustomUnits.NANOSECOND,
                                ParametersConfig.STANDARD_RESULTS_TIME_UNIT,
                                double.class)),
                        String.valueOf(supplyActivityOrder.getTiempoViajeCarga()),
                        String.valueOf(supplyActivityOrder.getHoraLlegadaViajeCarga()),
                        String.valueOf(supplyActivityOrder.getHoraInicioCarga()),
                        String.valueOf(supplyActivityOrder.getTiempoCarga()),
                        String.valueOf(supplyActivityOrder.getHoraFinCarga()),
                        String.valueOf(supplyActivityOrder.getHoraInicioViajeDescarga()),
                        String.valueOf(supplyActivityOrder.getTiempoViajeDescarga()),
                        String.valueOf(supplyActivityOrder.getHoraLlegadaViajeDescarga()),
                        String.valueOf(supplyActivityOrder.getHoraInicioDescarga()),
                        String.valueOf(supplyActivityOrder.getTiempoDescarga()),
                        String.valueOf(supplyActivityOrder.getHoraFinDescarga()),
                        String.valueOf(supplyActivityOrder.getPointNameSupplyActivityProposed()),
                        String.valueOf(supplyActivityOrder.getPointNameSupplyActivityRequired()),
                        String.valueOf(supplyActivityOrder.getPointNameSupplyActivityTransportation()),
                        String.valueOf(supplyActivity.getSupplyActivityRequired().getCantidadPersonaRequired()),
                        String.valueOf(supplyActivityOrder.getMaterialStock().getTotalAmountHelpByPerson()),
                        String.valueOf((double) CustomUnits.pipeStandarTime(
                                supplyActivity.getSupplyActivityOrder().getNegotiationTime().getElapsedTime(),
                                CustomUnits.NANOSECOND, double.class)),
                        String.valueOf(supplyActivity.getSupplyActivityProposed().getNegotiationQuantity()
                                + supplyActivity.getSupplyActivityTransportation().getNegotiationQuantity() + 1)
                };
                writer.writeNext(row);
            }

            header = new String[] { "numeroNegociaciones", "tiempoMinimoProcesamiento", "tiempoMaximoProcesamiento",
                    "tiempoPromedioProcesamiento", "tiempoTotal" };
            writer.writeNext(header);
            BigDecimal valor = BigDecimal.ZERO;
            for (SupplyActivity supplyActivity : activitieSupplyActivityList) {
                System.out.println(supplyActivity.getSupplyActivityOrder().getNegotiationTime().getElapsedTime());
                BigDecimal elapsedTime = BigDecimal
                        .valueOf(supplyActivity.getSupplyActivityOrder().getNegotiationTime().getElapsedTime());
                valor = valor.add(elapsedTime);
            }
            System.out.println("------");
            System.out.println(valor);
            String[] row = new String[] {
                    String.valueOf(activitieSupplyActivityList.stream()
                            .mapToInt(supplyActivity -> supplyActivity.getSupplyActivityProposed()
                                    .getNegotiationQuantity()
                                    + supplyActivity.getSupplyActivityTransportation().getNegotiationQuantity() + 1)
                            .sum()),
                    String.format("%f", activitieSupplyActivityList.stream()
                            .mapToDouble(supplyActivity -> supplyActivity.getSupplyActivityOrder().getNegotiationTime()
                                    .getElapsedTime())
                            .min().getAsDouble()),
                    String.format("%f", activitieSupplyActivityList.stream()
                            .mapToDouble(supplyActivity -> supplyActivity.getSupplyActivityOrder().getNegotiationTime()
                                    .getElapsedTime())
                            .max().getAsDouble()),
                    String.valueOf(activitieSupplyActivityList.stream()
                            .mapToDouble(supplyActivity -> supplyActivity.getSupplyActivityOrder().getNegotiationTime()
                                    .getElapsedTime())
                            .average()),
                    String.valueOf(activitieSupplyActivityList.stream()
                            .mapToDouble(supplyActivity -> supplyActivity.getSupplyActivityOrder().getNegotiationTime()
                                    .getElapsedTime())
                            .sum())
            };
            writer.writeNext(row);
            System.out.println("Archivo CSV generado con éxito.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
