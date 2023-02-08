package tomocomd.domainmethods;

import Jama.Matrix;
import ambit2.base.exceptions.AmbitIOException;
import ambit2.core.data.model.ModelWrapper;
import ambit2.core.io.DelimitedFileFormat;
import ambit2.core.io.FileInputState;
import ambit2.core.io.InteractiveIteratingMDLReader;
import ambit2.core.io.IteratingDelimitedFileReader;
import ambit2.model.numeric.DataCoverageDescriptors;
import lombok.AllArgsConstructor;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.IChemObjectReader;
import org.openscience.cdk.io.IChemObjectReaderErrorHandler;
import org.openscience.cdk.io.iterator.IIteratingChemObjectReader;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import silis.api.ambit.IProcessMolecule;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@AllArgsConstructor
public class DescriptorsAppDomainModel<DATA> extends ModelWrapper<File, File, File, DataCoverageDescriptors, String> {

    private static final Logger LOGGER = Logger.getLogger(DescriptorsAppDomainModel.class.getName());
    protected String[] header = null;
    protected File resultFile;

    DataCoverageDescriptors dataCoverageDescriptors;

    public void buildAD() throws Exception {
        int records = this.process((File) this.getTrainingInstances(), new IProcessMolecule() {
            int record = 0;

            public void processMolecule(IAtomContainer molecule) throws Exception {
                ++this.record;
            }
        });
        final Matrix matrix = new Matrix(records, this.header.length);
        this.process((File) this.getTrainingInstances(), new IProcessMolecule() {
            int record = 0;

            public void processMolecule(IAtomContainer molecule) throws Exception {
                for (int i = 0; i < header.length; ++i) {
                    Object v = molecule.getProperty(header[i]);
                    if (v != null) {
                        matrix.set(this.record, i, Double.parseDouble(v.toString()));
                    } else {
                        matrix.set(this.record, i, Double.NaN);
                    }
                }

                ++this.record;
            }
        });
        this.dataCoverageDescriptors.build(matrix);
    }

    private int process(File file, IProcessMolecule processor) throws Exception {
        if (file == null) {
            throw new Exception("ERROR: File not assigned! Use -t or -s options to set input files.");
        } else if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        } else {
            int records_read = 0;
            InputStream in = new FileInputStream(file);
            IIteratingChemObjectReader<IAtomContainer> reader = null;

            try {
                reader = getReader(in, file.getName());

                while(reader.hasNext()) {
                    IAtomContainer molecule = (IAtomContainer)reader.next();
                    ++records_read;
                    if (molecule != null) {
                        try {
                            processor.processMolecule(molecule);
                        } catch (Exception var17) {
                            System.err.println("*");
                            LOGGER.log(Level.SEVERE, String.format("[Record %d] Error %s\n", records_read, file.getAbsoluteFile()), var17);
                        }
                    }
                }
            } catch (CDKException | AmbitIOException var18) {
                LOGGER.log(Level.SEVERE, String.format("[Record %d] Error %s\n", records_read, file.getAbsoluteFile()), var18);
            } finally {
                try {
                    reader.close();
                } catch (IOException var16) {
                }

            }

            return records_read;
        }
    }
    private IIteratingChemObjectReader<IAtomContainer> getReader(InputStream in, String extension) throws AmbitIOException, CDKException, UnsupportedEncodingException {
        Object reader =  new IteratingDelimitedFileReader(in, new DelimitedFileFormat(",", '"'));


        ((IIteratingChemObjectReader)reader).setReaderMode(IChemObjectReader.Mode.RELAXED);
        ((IIteratingChemObjectReader)reader).setErrorHandler(new IChemObjectReaderErrorHandler() {
            public void handleError(String message, int row, int colStart, int colEnd, Exception exception) {
                LOGGER.log(Level.SEVERE, String.format("Error at row %d col %d - %d %s", row, colStart, colEnd, exception.getMessage()));
            }

            public void handleError(String message, int row, int colStart, int colEnd) {
                LOGGER.log(Level.SEVERE, String.format("Error at row %d col %d - %d %s", row, colStart, colEnd, message));
            }

            public void handleError(String message, Exception exception) {
                LOGGER.log(Level.SEVERE, message, exception);
            }

            public void handleError(String message) {
                LOGGER.log(Level.SEVERE, message);
            }
        });
        return (IIteratingChemObjectReader)reader;
    }

    public String[] estimateAD(int pos, String[] values) throws Exception {
        Matrix matrix = new Matrix(1, values.length - pos);

        for(int i = pos; i < values.length; ++i) {
            matrix.set(0, i - pos, values[i].equals("NaN") ? 0.0 : Double.parseDouble(values[i]));
        }

        double originalPre = this.dataCoverageDescriptors.predict(matrix)[0];
        double roundPre = (double)Math.round(originalPre * 1000.0);
        roundPre /= 1000.0;
        return new String[]{Double.toString(roundPre), this.dataCoverageDescriptors.getDomain(originalPre) == 0 ? "IN" : "OUT"};
    }
}
