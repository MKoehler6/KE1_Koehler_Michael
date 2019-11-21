package propra.imageconverter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author Michael Köhler
 * Eine Instanz dieser Klasse übernimmt die Konvertierung 
 * vom TGA-Format in das Propra-Format
 *
 */
public class ConverterTgaToPropra implements IConverter {
	
	private IDataModel dataModel;
	private byte[] dataInputArray;
	private byte[] dataOutputArray;

	/* 
	 *  überprüft alle Anforderungen an die TGA Datei und die Korrektheit der Daten im Header
	 */
	public void checkData(IDataModel dataModel) throws ConverterException {
		this.dataModel = dataModel;
		dataInputArray = dataModel.getDataInputArray();
//		prüfe optionale Anforderungen an TGA-Datei
		checkImageDimensions(); // Bildbreite oder -höhe dürfen nicht 0 sein
		checkTypeOfCompression(); // Kompressionstyp muss 2 sein
		checkCorrectAmountOfPixel();
//		prüfe, ob geforderte Einschränkungen für TGA-Dateien eingehalten werden
		checkBitsPerPixel(); // nur 24Bit zugelasen
		checkTypeOfImage(); // nur Bildtyp 2
		checkAttributeByte(); // vertikale Lage des Nullpunktes, Wert muss 32 betragen
		checkLengthOfImageID(); // muss Null sein
	}

	private void checkImageDimensions() throws ConverterException {
		if ((dataInputArray[12] == 0 && dataInputArray[13] == 0) ||
				(dataInputArray[14] == 0 && dataInputArray[15] == 0)) 
			throw new ConverterException("mind. eine Bilddimension ist 0");
	}

	private void checkTypeOfCompression() throws ConverterException {
		if (dataInputArray[2] != 2) throw new ConverterException("nicht unterstützter Bildtyp");
	}

	/**
	 * überprüft, ob die Anzahl der Pixel mit den Angaben im Header übereinstimmen
	 * entfernt einen eventuell vorhandenen Dateifuß
	 */
	private void checkCorrectAmountOfPixel() throws ConverterException{
		byte[] dataSegment = getDataSegment();
//		erstellt ein Byte-Array der Länge 4 mit den Bildbreite-Daten aus dem Header
//		und berechnet daraus die Bildbreite als Integer
		byte[] widthArray = {dataInputArray[12], dataInputArray[13],0,0};
		int width = byteToInt4(widthArray); 
//		erstellt ein Byte-Array der Länge 4 mit den Bildhöhe-Daten aus dem Header
//		und berechnet daraus die Bildhöhe als Integer
		byte[] heightArray = {dataInputArray[14], dataInputArray[15],0,0};
		int height = byteToInt4(heightArray); 
		if (dataSegment.length < width * height * 3) throw new ConverterException("Fehlende Pixeldaten."
				+ " Stimmen nicht mit Breite x Höhe im Header überein");
		if (dataSegment.length > width * height * 3) {
			// entferne Dateifuß
			byte[] temp = new byte[width * height * 3 + 18];
			for (int i = 0; i < temp.length; i++) {
				temp[i] = dataInputArray[i];
			}
			dataInputArray = temp;
		}
	}

	private void checkBitsPerPixel() throws ConverterException {
		if (dataInputArray[16] != 24) throw new ConverterException("Bits pro Pixel nicht korrekt");
	}
	
	private void checkTypeOfImage() throws ConverterException {
		if (dataInputArray[2] != 2) throw new ConverterException("Bildtyp nicht korrekt");
	}
	
	private void checkAttributeByte() throws ConverterException {
		if (dataInputArray[17] != 32) throw new ConverterException("Bild-Attribut-Byte nicht korrekt");
	}

	private void checkLengthOfImageID() throws ConverterException {
		if (dataInputArray[0] != 0) throw new ConverterException("Länge der Bild-ID nicht korrekt");
	}
	
	/* 
	 * konvertiert den Header und die Pixel in das Propra-Format
	 */
	public void convert() {
		byte[] dataSegment = getDataSegment();
		dataOutputArray = new byte[28 + dataSegment.length];
		for (int i = 0; i < 28; i++) { // Header mit 0 befüllen
			dataOutputArray[i] = 0;
		}
		dataOutputArray[0] = 80;
		dataOutputArray[1] = 114;
		dataOutputArray[2] = 111;
		dataOutputArray[3] = 80;
		dataOutputArray[4] = 114;
		dataOutputArray[5] = 97;
		dataOutputArray[6] = 87;
		dataOutputArray[7] = 83;
		dataOutputArray[8] = 49;
		dataOutputArray[9] = 57;
		dataOutputArray[10] = dataInputArray[12]; 
		dataOutputArray[11] = dataInputArray[13];
		dataOutputArray[12] = dataInputArray[14]; 
		dataOutputArray[13] = dataInputArray[15];
		dataOutputArray[14] = 24; 
		dataOutputArray[15] = 0;
		// berechnet die Länge des Datensegments und gibt sie als Byte-Array der Länge 8 zurück
		byte[] lengthOfDatasegmentByteArray = getLengthOfDatasegmentByteArray(dataSegment.length);
		for (int i = 0; i < 8; i++) {
			dataOutputArray[16 + i] = lengthOfDatasegmentByteArray[7-i];
		}
		// konvertiert das Datensegment
		for (int i = 0; i < dataSegment.length; i++) {
			if (i % 3 == 0) dataOutputArray[28 + i] = dataSegment[i + 1];
			if (i % 3 == 1) dataOutputArray[28 + i] = dataSegment[i - 1];
			if (i % 3 == 2) dataOutputArray[28 + i] = dataSegment[i];
		}
		// berechnet Prüfsumme des konvertierten Datensegment
		byte[] dataSegmentAfterConvert = getDataSegmentAfterConvert();
		long checkSum = calculateCheckSum(dataSegmentAfterConvert);
		// erstellt aus der Prüfsumme ein byte-Array
		byte[] chekSumByteArray = longToBytes(checkSum); // erzeugt ein 8 Byte großes Byte-Array
		for (int i = 0; i < 4; i++) {
			dataOutputArray[24+i] = chekSumByteArray[7-i]; // schreibt die letzten 4 Bytes von
		}													// chekSumByteArray in umgekehrter Reihenfolge
															// in den Header
		dataModel.setDataOutputArraySize(dataOutputArray.length);
		dataModel.setDataOutputArray(dataOutputArray);
	}
	
	/**
	 * gibt Länge des Datensegments als Byte-Array der Länge 8 zurück
	 */
	private byte[] getLengthOfDatasegmentByteArray(int length) {
		byte[] temp = longToBytes((long)length);
		return temp;
	}

	/**
	 * gibt ein Byte-Array mit den Daten des Datensegments zurück 
	 */
	private byte[] getDataSegment() {
		byte[] data = new byte[dataInputArray.length - 18];
		for (int i = 18; i < dataInputArray.length; i++) {
			data[i-18] = dataInputArray[i];
		}
		return data;
	}
	
	/**
	 * gibt ein Byte-Array mit den Daten des Datensegments nach der Konvertierung zurück  
	 */
	private byte[] getDataSegmentAfterConvert() {
		byte[] data = new byte[dataOutputArray.length - 28];
		for (int i = 28; i < dataOutputArray.length; i++) {
			data[i-28] = dataOutputArray[i];
		}
		return data;
	}
	
	private long calculateCheckSum(byte[] dataSegment) {
		long an = 0;
		long bn = 1;
		int x = 65513;
		for (int i = 0; i < dataSegment.length; i++) {
			an = an + (Byte.toUnsignedInt(dataSegment[i])+i+1);
			bn = (bn + an % x) % x;
		}
		an = an % x;
		return an*65536 + bn;
	}
	
	/**
	 * erzeugt ein 8 Byte großes Byte-Array aus einer long
	 */
	private byte[] longToBytes(long x) { 
	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	    buffer.putLong(x);
	    return buffer.array();
	}
	
	/**
	 * berechnet aus einem Byte-Array der Länge 4 im Little-Endian-Format einen Integer-Wert
	 */
	private int byteToInt4(byte[] data) { 
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer.getInt();
	}

}
