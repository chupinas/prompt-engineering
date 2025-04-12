package com.epam.training.gen.ai.examples.semantic.model;

import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordKey;
import com.microsoft.semantickernel.data.vectorstorage.annotations.VectorStoreRecordVector;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class DummyRecord {
    @VectorStoreRecordKey
    private  String id;
    @VectorStoreRecordVector(dimensions = 1536)
    private  List<Float> embedding;

    public DummyRecord(String id, List<Float> embedding){
        this.id = id;
        this.embedding = embedding;
    }

    public String getId() {
        return id;
    }

    public List<Float> getEmbedding() {
        return embedding;
    }

    public static String encodeId(String realId) {
        byte[] bytes = Base64.getUrlEncoder().encode(realId.getBytes(StandardCharsets.UTF_8));
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
