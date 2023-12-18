package src.commons;
import com.opencsv.CSVWriterBuilder;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.opencsv.ICSVWriter;

import src.models.SupplyActivity;
import src.models.SupplyActivityOrder;

public class FileGenerator {
    String pathFolder = "./src/results/";

    // String csvFilePath = "ruta/del/archivo.csv";
    public void generateFile(String nameDocument,List<SupplyActivity> activitieSupplyActivityList) {
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
                    "pointNameSupplyActivityTransportation", "ayudaRequerida", "ayudaTransportada" };
            writer.writeNext(header);
            for (SupplyActivity supplyActivity : activitieSupplyActivityList) {
                SupplyActivityOrder supplyActivityOrder = supplyActivity.getSupplyActivityOrder();
                String[] row = {
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
                        String.valueOf(supplyActivityOrder.getPointNameSupplyActivityProposed()),
                        String.valueOf(supplyActivityOrder.getPointNameSupplyActivityRequired()),
                        String.valueOf(supplyActivityOrder.getPointNameSupplyActivityTransportation()),
                        String.valueOf(supplyActivity.getSupplyActivityRequired().getCantidadPersonas()),
                        String.valueOf(supplyActivityOrder.getMaterialStock().getTotalAmountHelpByPerson())
                };
                writer.writeNext(row);
            }
            System.out.println("Archivo CSV generado con éxito.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
