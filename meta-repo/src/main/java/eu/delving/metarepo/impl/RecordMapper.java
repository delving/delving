package eu.delving.metarepo.impl;

import eu.delving.metarepo.core.MetaRepo;
import eu.europeana.definitions.annotations.AnnotationProcessor;
import eu.europeana.sip.core.ConstantFieldModel;
import eu.europeana.sip.core.MappingRunner;
import eu.europeana.sip.core.MetadataRecord;
import eu.europeana.sip.core.ToolCodeModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Run the a mapping on a number of records
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class RecordMapper {
    private String code;
    private AnnotationProcessor annotationProcessor;

    public RecordMapper(
            String code,
            AnnotationProcessor annotationProcessor
    ) {
        this.code = code;
        this.annotationProcessor = annotationProcessor;
    }

    public List<? extends MetaRepo.Record> map(List<? extends MetaRepo.Record> records) {
        ConstantFieldModel constantFieldModel = ConstantFieldModel.fromMapping(code, annotationProcessor);
        ToolCodeModel toolCodeModel = new ToolCodeModel();
        final List<? extends MetaRepo.Record> outputList = new ArrayList<MetaRepo.Record>();
        MappingRunner mappingRunner = new MappingRunner(toolCodeModel.getCode() + code, constantFieldModel, new MappingRunner.Listener() {
            @Override
            public void complete(MetadataRecord metadataRecord, Exception exception, String output) {
                if (exception != null) {

                }
                else {
//                        outputList.add()
                }
            }
        });
        for (MetaRepo.Record record : records) {
            MetadataRecord metadataRecord = record.metadataRecord();
            mappingRunner.runMapping(metadataRecord);
        }
        return outputList;
    }
}