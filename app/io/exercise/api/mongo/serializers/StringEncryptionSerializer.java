package io.exercise.api.mongo.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.base.Strings;
import io.exercise.api.mongo.encryptography.EncryptionUtil;
import lombok.SneakyThrows;
import play.Logger;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Base64;

public class StringEncryptionSerializer extends JsonSerializer<String> {

    @SneakyThrows
    @Override
    public void serialize(String value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        if (value == null) {
            jgen.writeNull();
        }

        Object publicKeyAttr = provider.getConfig().getAttributes().getAttribute(SerializationAttributes.PUBLIC_KEY_ATTRIBUTE);
        Object typeAttr = provider.getConfig().getAttributes().getAttribute(SerializationAttributes.ENCRYPTION_TYPE_ATTRIBUTE);
        if (publicKeyAttr == null || typeAttr == null) {
            Logger.of(this.getClass()).warn("Encryption attributes missing");
            jgen.writeString(value);
            return;
        }
        byte[] decoded = Base64.getDecoder().decode(publicKeyAttr.toString());
        String type = typeAttr.toString();
        if (decoded.length == 0 || Strings.isNullOrEmpty(type)) {
            Logger.of(this.getClass()).warn("Encryption attributes empty");
            jgen.writeString(value);
            return;
        }
        try {
            EncryptionUtil encryptionUtil = new EncryptionUtil(type);
            PublicKey key = encryptionUtil.getPublicKeyFromBytes(decoded, type);
            Logger.of(this.getClass()).debug("Encrypted string {} from {}", encryptionUtil.encryptText(value, key), value);
            jgen.writeString(encryptionUtil.encryptText(value, key));
            return;
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.of(this.getClass()).warn("Failed to encrypt");
        }
        jgen.writeString(value);
    }


}

