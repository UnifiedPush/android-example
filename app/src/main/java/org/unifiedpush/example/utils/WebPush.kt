package org.unifiedpush.example.utils

/* ktlint-disable no-wildcard-imports */
import android.util.Base64
import com.google.crypto.tink.subtle.EllipticCurves
import java.security.*
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
/* ktlint-enable no-wildcard-imports */

data class SerializedKeyPair(
    val private: String,
    val public: String
)

/**
 * https://www.rfc-editor.org/rfc/rfc8291
*/

object WebPush {

    fun generateKeyPair(): KeyPair {
        return KeyPairGenerator.getInstance("EC").apply {
            initialize(
                ECGenParameterSpec("secp256r1")
            )
        }.generateKeyPair()
    }

    fun generateAuthSecret(): ByteArray {
        return ByteArray(16).apply {
            SecureRandom().nextBytes(this)
        }
    }

    fun encodeKeyPair(keyPair: KeyPair): SerializedKeyPair {
        return SerializedKeyPair(
            private = b64encode(keyPair.private.encoded),
            public = b64encode(keyPair.public.encoded)
        )
    }

    fun decodePubKey(raw: String): ECPublicKey {
        val kf = KeyFactory.getInstance("EC")
        val publicBytes = b64decode(raw)
        val publicSpec = X509EncodedKeySpec(publicBytes)
        return kf.generatePublic(publicSpec) as ECPublicKey
    }

    fun decodeKeyPair(serializedKeyPair: SerializedKeyPair): KeyPair {
        val kf = KeyFactory.getInstance("EC")

        val privateBytes = b64decode(serializedKeyPair.private)
        val privateSpec = PKCS8EncodedKeySpec(privateBytes)
        val privateKey = kf.generatePrivate(privateSpec)

        val publicKey = decodePubKey(serializedKeyPair.public)
        return KeyPair(publicKey, privateKey)
    }

    fun serializePublicKey(publicKey: ECPublicKey): String {
        return b64encode(
            EllipticCurves.pointEncode(EllipticCurves.CurveType.NIST_P256, EllipticCurves.PointFormatType.UNCOMPRESSED, publicKey.w)
        )
    }

    fun b64encode(byteArray: ByteArray): String {
        return Base64.encodeToString(
            byteArray,
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )
    }

    fun b64decode(string: String): ByteArray {
        return Base64.decode(
            string,
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )
    }
}
