package src.commons;

import com.opencsv.CSVWriterBuilder;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.opencsv.ICSVWriter;

import src.models.SupplyActivity;
import src.models.SupplyActivityOrder;
import src.models.SupplyActivityRequired;

public class FileGenerator {
        String pathFolder = "./src/results/";

        // String csvFilePath = "ruta/del/archivo.csv";
        public void generateFile(String nameDocument, List<SupplyActivity> activitieSupplyActivityList) {
                String filePath = pathFolder.concat(nameDocument).concat("_results.csv");
                char delimiter = ';';
                char quotechar = '\0';
                String[] row;
                try (ICSVWriter writer = new CSVWriterBuilder(new FileWriter(filePath))
                                .withSeparator(delimiter)
                                .withQuoteChar(quotechar)
                                .build()) {
                        String[] header = { "horaInicioViajeCarga", "tiempoViajeCarga", "horaLlegadaViajeCarga",
                                        "horaInicioCarga",
                                        "tiempoCarga", "horaFinCarga", "horaInicioViajeDescarga", "tiempoViajeDescarga",
                                        "horaLlegadaViajeDescarga", "horaInicioDescarga", "tiempoDescarga",
                                        "horaFinDescarga",
                                        "pointNameSupplyActivityProposed", "pointNameSupplyActivityRequired",
                                        "pointNameSupplyActivityTransportation", "ayudaRequerida", "ayudaTransportada",
                                        "tiempoDuraciónConversacionRequerida (ms)",
                                        "tiempoDuraciónConversacionPropuesta (ms)",
                                        "tiempoDuraciónConversacionTransporte (ms)",
                                        "tiempoDuraciónConversacionTotal (ms)",
                                        "cantidadNegociacionesTotal" };
                        writer.writeNext(header);
                        for (SupplyActivity supplyActivity : activitieSupplyActivityList) {
                                SupplyActivityOrder supplyActivityOrder = supplyActivity.getSupplyActivityOrder();
                                row = new String[] {
                                                String.valueOf(supplyActivityOrder.getHoraInicioViajeCarga()),
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
                                                String.valueOf(supplyActivityOrder
                                                                .getPointNameSupplyActivityProposed()),
                                                String.valueOf(supplyActivityOrder
                                                                .getPointNameSupplyActivityRequired()),
                                                String.valueOf(supplyActivityOrder
                                                                .getPointNameSupplyActivityTransportation()),
                                                String.valueOf(supplyActivity.getSupplyActivityRequired()
                                                                .getCantidadPersonaRequired()),
                                                String.valueOf(supplyActivityOrder.getMaterialStock()
                                                                .getTotalAmountHelpByPerson()),
                                                String.valueOf(supplyActivity.getSupplyActivityRequired()
                                                                .getNegotiationTime().getElapsedTime()),
                                                String.valueOf(supplyActivity.getSupplyActivityProposed()
                                                                .getNegotiationTime().getElapsedTime()),
                                                String.valueOf(supplyActivity.getSupplyActivityTransportation()
                                                                .getNegotiationTime().getElapsedTime()),
                                                String.valueOf(supplyActivity.getSupplyActivityOrder()
                                                                .getNegotiationTime().getElapsedTime()),
                                                String.valueOf(supplyActivity.getSupplyActivityProposed()
                                                                .getNegotiationQuantity()
                                                                + supplyActivity.getSupplyActivityTransportation()
                                                                                .getNegotiationQuantity()
                                                                + 1)
                                };
                                writer.writeNext(row);
                        }
                        System.out.println("Archivo CSV generado con éxito.");
                        // header = new String[] { "numeroNegociaciones", "tiempoMinimoProcesamiento",
                        // "tiempoMaximoProcesamiento",
                        // "tiempoPromedioProcesamiento", "tiempoTotal" };
                        // writer.writeNext(header);
                        // row = new String[] {
                        // String.valueOf(activitieSupplyActivityList.stream()
                        // .mapToInt(supplyActivity -> supplyActivity
                        // .getSupplyActivityOrder()
                        // .getNegotiationQuantity())
                        // .sum()),
                        // String.valueOf(CustomUnits.pipeStandarTime(
                        // Double.valueOf(activitieSupplyActivityList.stream()
                        // .mapToDouble(supplyActivity -> supplyActivity
                        // .getSupplyActivityOrder()
                        // .getNegotiationTime()
                        // .getElapsedTime())
                        // .min().orElse(0.0)),
                        // CustomUnits.NANOSECOND, CustomUnits.MILLISECOND,
                        // long.class)),
                        // String.valueOf(CustomUnits.pipeStandarTime(
                        // Double.valueOf(activitieSupplyActivityList.stream()
                        // .mapToDouble(supplyActivity -> supplyActivity
                        // .getSupplyActivityOrder()
                        // .getNegotiationTime()
                        // .getElapsedTime())
                        // .max().orElse(0.0)),
                        // CustomUnits.NANOSECOND, CustomUnits.MILLISECOND,
                        // long.class)),
                        // String.valueOf(CustomUnits.pipeStandarTime(
                        // Double.valueOf(activitieSupplyActivityList.stream()
                        // .mapToDouble(supplyActivity -> supplyActivity
                        // .getSupplyActivityOrder()
                        // .getNegotiationTime()
                        // .getElapsedTime())
                        // .average().orElse(0.0)),
                        // CustomUnits.NANOSECOND, CustomUnits.MILLISECOND,
                        // long.class)),
                        // String.valueOf(CustomUnits.pipeStandarTime(
                        // activitieSupplyActivityList.stream()
                        // .mapToDouble(supplyActivity -> supplyActivity
                        // .getSupplyActivityOrder()
                        // .getNegotiationTime()
                        // .getElapsedTime())
                        // .reduce(Double::sum)
                        // .orElse(0.0),
                        // CustomUnits.NANOSECOND, CustomUnits.MILLISECOND,
                        // long.class))

                        // };
                        // writer.writeNext(row);
                        // // Suponiendo que activitieSupplyActivityList ya está definida
                        // Map<Long, Integer> sumByStartHour = activitieSupplyActivityList.stream()
                        // .collect(Collectors.groupingBy(
                        // supplyActivity -> supplyActivity.getSupplyActivityOrder()
                        // .getHoraInicioViajeCarga(),
                        // TreeMap::new, // Usar TreeMap para asegurar el orden natural de
                        // // las claves
                        // Collectors.summingInt(supplyActivity -> supplyActivity
                        // .getSupplyActivityProposed()
                        // .getNegotiationQuantity() +
                        // supplyActivity.getSupplyActivityTransportation()
                        // .getNegotiationQuantity()
                        // + 1)));

                        // header = new String[] { "horaInicioActividad",
                        // "sumatoriaCantidadNegociacionesTotalPeriodo" };
                        // writer.writeNext(header);

                        // for (Map.Entry<Long, Integer> entry : sumByStartHour.entrySet()) {
                        // row = new String[] {
                        // String.valueOf(entry.getKey()),
                        // String.valueOf(entry.getValue()),
                        // };
                        // writer.writeNext(row);
                        // }
                        System.out.println("Archivo CSV generado con éxito.");
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }

        public void generateFileDisaggregated(String nameDocument, List<SupplyActivity> activitieSupplyActivityList) {
                String filePath = pathFolder.concat(nameDocument).concat("_RESULTS_DISAGGREGATED.csv");
                char delimiter = ';';
                char quotechar = '\0';
                try (ICSVWriter writer = new CSVWriterBuilder(new FileWriter(filePath))
                                .withSeparator(delimiter)
                                .withQuoteChar(quotechar)
                                .build()) {
                        String[] header = { "idActivity", "activityType", "durationTime", "quantityNegotiation",
                                        "helpRequire" };
                        writer.writeNext(header);
                        for (SupplyActivity supplyActivity : activitieSupplyActivityList) {
                                writer.writeNext(new String[] {
                                                String.valueOf(supplyActivity.hashCode()),
                                                String.valueOf(ParametersConfig.NAME_ACTIVITY_REQUIRED),
                                                String.valueOf(supplyActivity.getSupplyActivityRequired()
                                                                .getNegotiationTime().getElapsedTime()),
                                                String.valueOf(1),
                                                String.valueOf(supplyActivity.getSupplyActivityRequired()
                                                                .getCantidadPersonaRequired()),
                                });
                                writer.writeNext(new String[] {
                                                String.valueOf(supplyActivity.hashCode()),
                                                String.valueOf(ParametersConfig.NAME_ACTIVITY_PROPOSED),
                                                String.valueOf(supplyActivity.getSupplyActivityProposed()
                                                                .getNegotiationTime().getElapsedTime()),
                                                String.valueOf(supplyActivity.getSupplyActivityProposed()
                                                                .getNegotiationQuantity()),
                                                String.valueOf(supplyActivity.getSupplyActivityRequired()
                                                                .getCantidadPersonaRequired())
                                });
                                writer.writeNext(new String[] {
                                                String.valueOf(supplyActivity.hashCode()),
                                                String.valueOf(ParametersConfig.NAME_ACTIVITY_TRANSPORTATION),
                                                String.valueOf(supplyActivity.getSupplyActivityTransportation()
                                                                .getNegotiationTime().getElapsedTime()),
                                                String.valueOf(supplyActivity.getSupplyActivityTransportation()
                                                                .getNegotiationQuantity()),
                                                String.valueOf(supplyActivity.getSupplyActivityRequired()
                                                                .getCantidadPersonaRequired())
                                });
                        }
                        System.out.println("Archivo CSV generado con éxito.");
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }
}
