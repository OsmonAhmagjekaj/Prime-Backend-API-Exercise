package io.exercise.api.mongo.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import io.exercise.api.mongo.encryptography.EncryptionUtil;
import lombok.SneakyThrows;
import play.Logger;

import java.io.IOException;
import java.security.PrivateKey;
import java.util.Base64;


public class StringDecryptionDeSerializer extends JsonDeserializer<String> {

    @SneakyThrows
    @Override
    public String deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        if (node.isNull()) {
            return null;
        }
        if (!node.isTextual()) {
            return null;
        }
        String text = node.asText();
        Object privateKeyAttr = ctxt.getConfig().getAttributes().getAttribute(SerializationAttributes.PRIVATE_KEY_ATTRIBUTE);
        Object typeAttr = ctxt.getConfig().getAttributes().getAttribute(SerializationAttributes.ENCRYPTION_TYPE_ATTRIBUTE);
        if (privateKeyAttr == null || typeAttr == null) {
            Logger.of(this.getClass()).warn("Encryption Attributes missing");
            return text;
        }
        byte[] decoded = Base64.getDecoder().decode(privateKeyAttr.toString());
        String type = typeAttr.toString();
        if (decoded.length == 0 || Strings.isNullOrEmpty(type)) {
            Logger.of(this.getClass()).warn("Encryption Attributes empty");
            return text;
        }
        try {
            EncryptionUtil encryptionUtil = new EncryptionUtil(type);
            PrivateKey key = encryptionUtil.getPrivateKeyFromBytes(decoded, type);
            return encryptionUtil.decryptText(text, key);
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.of(this.getClass()).warn("Failed to decrypt, continue with raw text");
            return text;
        }
    }
}
