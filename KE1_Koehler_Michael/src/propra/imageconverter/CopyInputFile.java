package propra.imageconverter;

/**
 * @author Michael Köhler
 * Eine Instanz dieser Klasse kopiert die Daten vom dataInputArray in das
 * dataOutputArray
 *
 */
public class CopyInputFile {
	
	private IDataModel dataModel;
	private byte[] dataInputArray;
	
	public CopyInputFile (IDataModel dataModel) {
		this.dataModel = dataModel;
	}
	
	public void copy() {
		dataInputArray = dataModel.getDataInputArray();
		dataModel.setDataOutputArraySize(dataInputArray.length);
		dataModel.setDataOutputArray(dataInputArray);
	}

}
