/*
 * Copyright (C) 2018 nyris GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nyris.sdk

import android.support.annotation.FloatRange
import android.support.annotation.IntRange
import android.util.Base64
import com.google.gson.Gson
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.RequestBody
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * ImageMatchingApi.kt - class that implement IImageMatchingApi interface.
 * @see IImageMatchingApi
 *
 * @author Sidali Mellouk
 * Created by nyris GmbH
 * Copyright © 2018 nyris GmbH. All rights reserved.
 */
internal class ImageMatchingApi(private val imageMatchingService: ImageMatchingService,
                                private var outputFormat: String,
                                private var language: String,
                                private var gson: Gson,
                                schedulerProvider: SdkSchedulerProvider,
                                apiHeader: ApiHeader,
                                endpoints: EndpointBuilder) : Api(schedulerProvider, apiHeader, endpoints), IImageMatchingApi {

    private val exactOptions: ExactOptions = ExactOptions()
    private val similarityOptions: SimilarityOptions = SimilarityOptions()
    private val ocrOptions: OcrOptions = OcrOptions()
    private val regroupOptions: RegroupOptions = RegroupOptions()
    private val recommendationOptions: RecommendationOptions = RecommendationOptions()
    private val categoryPredictionOptions: CategoryPredictionOptions = CategoryPredictionOptions()
    private var limit: Int = 20

    /**
     * Init local properties
     */
    private fun reset() {
        exactOptions.reset()
        similarityOptions.reset()
        ocrOptions.reset()
        recommendationOptions.reset()
        categoryPredictionOptions.reset()
        limit = 20
    }

    /**
     * {@inheritDoc}
     */
    override fun outputFormat(outputFormat: String): ImageMatchingApi {
        this.outputFormat = outputFormat
        return this
    }

    /**
     * {@inheritDoc}
     */
    override fun language(language: String): ImageMatchingApi {
        this.language = language
        return this
    }

    /**
     * {@inheritDoc}
     */
    override fun exact(action: ExactOptions.() -> Unit): IImageMatchingApi {
        action(exactOptions)
        return this
    }

    override fun exact(isEnabled: Boolean): IImageMatchingApi {
        exactOptions.enabled = isEnabled
        return this
    }

    /**
     * {@inheritDoc}
     */
    override fun similarity(isEnabled: Boolean): IImageMatchingApi {
        similarityOptions.enabled = isEnabled
        return this
    }

    /**
     * {@inheritDoc}
     */
    override fun similarity(action: SimilarityOptions.() -> Unit): IImageMatchingApi {
        action(similarityOptions)
        return this
    }

    /**
     * {@inheritDoc}
     */
    override fun similarityLimit(@IntRange(from = 1, to = 100) limit: Int): IImageMatchingApi {
        similarityOptions.limit = limit
        return this
    }

    /**
     * {@inheritDoc}
     */
    override fun similarityThreshold(@FloatRange(from = 0.0, to = 1.0) threshold: Float): IImageMatchingApi {
        similarityOptions.threshold = threshold
        return this
    }

    /**
     * {@inheritDoc}
     */
    override fun ocr(isEnabled: Boolean): IImageMatchingApi {
        ocrOptions.enabled = isEnabled
        return this
    }

    /**
     * {@inheritDoc}
     */
    override fun ocr(action: OcrOptions.() -> Unit): IImageMatchingApi {
        action(ocrOptions)
        return this
    }

    /**
     * {@inheritDoc}
     */
    override fun limit(limit: Int): IImageMatchingApi {
        this.limit = limit
        return this
    }

    /**
     * {@inheritDoc}
     */
    override fun regroup(isEnabled: Boolean): IImageMatchingApi {
        regroupOptions.enabled = isEnabled
        return this
    }

    /**
     * {@inheritDoc}
     */
    override fun regroup(action: RegroupOptions.() -> Unit): IImageMatchingApi {
        action(regroupOptions)
        return this
    }

    /**
     * {@inheritDoc}
     */
    override fun regroupThreshold(@FloatRange(from = 0.0, to = 1.0) threshold: Float): IImageMatchingApi {
        regroupOptions.threshold = threshold
        return this
    }

    /**
     * {@inheritDoc}
     */
    override fun recommendations(isEnabled: Boolean): IImageMatchingApi {
        recommendationOptions.enabled = isEnabled
        return this
    }

    /**
     * {@inheritDoc}
     */
    override fun recommendations(action: RecommendationOptions.() -> Unit): IImageMatchingApi {
        action(recommendationOptions)
        return this
    }

    /**
     * {@inheritDoc}
     */
    override fun categoryPrediction(isEnabled: Boolean): IImageMatchingApi {
        categoryPredictionOptions.enabled = isEnabled
        return this
    }

    override fun categoryPrediction(action: CategoryPredictionOptions.() -> Unit): IImageMatchingApi {
        action(categoryPredictionOptions)
        return this
    }

    /**
     * {@inheritDoc}
     */
    override fun categoryPredictionLimit(limit: Int): IImageMatchingApi {
        categoryPredictionOptions.limit = limit
        return this
    }

    /**
     * {@inheritDoc}
     */
    override fun categoryPredictionThreshold(threshold: Float): IImageMatchingApi {
        categoryPredictionOptions.threshold = threshold
        return this
    }

    /**
     * {@inheritDoc}
     */
    override fun buildXOptions(): String {
        var xOptions = ""

        if (exactOptions.enabled && xOptions.isEmpty()) xOptions = "exact"

        if (similarityOptions.enabled && xOptions.isEmpty()) xOptions = "similarity"
        else
            if (similarityOptions.enabled) xOptions += " +similarity"

        if (ocrOptions.enabled && xOptions.isEmpty()) xOptions = "ocr"
        else
            if (ocrOptions.enabled) xOptions += " +ocr"

        if (similarityOptions.enabled && similarityOptions.limit != -1) xOptions += " similarity.limit=${similarityOptions.limit}"

        if (similarityOptions.enabled && similarityOptions.threshold != -1F) xOptions += " similarity.threshold=${similarityOptions.threshold}"

        if (regroupOptions.enabled) xOptions += " +regroup"

        if (regroupOptions.enabled && regroupOptions.threshold != -1F) xOptions += " regroup.threshold=${regroupOptions.threshold}"

        if (limit != 20) xOptions += " limit=$limit"

        if (recommendationOptions.enabled) xOptions += " +recommendations"

        if (categoryPredictionOptions.enabled) xOptions += " +category-prediction"

        if (categoryPredictionOptions.enabled && categoryPredictionOptions.limit != -1) xOptions += " category-prediction.limit=${categoryPredictionOptions.limit}"

        if (categoryPredictionOptions.enabled && categoryPredictionOptions.threshold != -1F) xOptions += " category-prediction.threshold=${categoryPredictionOptions.threshold}"

        reset()
        return xOptions
    }

    /**
     * Build Headers for image matching endpoint
     */
    private fun buildHeaders(contentSize: Int): HashMap<String, String> {
        val headers = createDefaultHeadersMap()
        headers["Accept"] = outputFormat
        headers["Accept-Language"] = language
        headers["Content-Length"] = contentSize.toString()
        headers["X-Options"] = buildXOptions()
        return headers
    }

    /**
     * {@inheritDoc}
     */
    override fun match(image: ByteArray): Single<OfferResponseBody> {
        return match(image, OfferResponseBody::class.java)
    }

    /**
     * {@inheritDoc}
     */
    override fun match(image: FloatArray): Single<OfferResponseBody> {
        return match(image, OfferResponseBody::class.java)
    }

    private fun encodeFloatArray(floatArray: FloatArray): String {
        val buf = ByteBuffer
                .allocate(java.lang.Float.SIZE / java.lang.Byte.SIZE * floatArray.size)
                .order(ByteOrder.LITTLE_ENDIAN)
        buf.asFloatBuffer().put(floatArray)
        return Base64.encodeToString(buf.array(), Base64.NO_WRAP)
    }

    /**
     * {@inheritDoc}
     */
    override fun <T : IResponse> match(image: ByteArray, clazz: Class<T>): Single<T> {
        if (recommendationOptions.enabled && !ternaryOr(exactOptions.enabled, similarityOptions.enabled, ocrOptions.enabled)) {
            val exception = Exception("To use the recommendation feature, you need to enable one of this stages : exact, similarity, ocr.")
            return Single.error<T>(exception)
        }

        if (regroupOptions.enabled && !ternaryOr(exactOptions.enabled, similarityOptions.enabled, ocrOptions.enabled)) {
            val exception = Exception("To use the regrouping feature, you need to enable one of this stages : exact, similarity, ocr.")
            return Single.error<T>(exception)
        }

        val headers = buildHeaders(image.size)
        val body = RequestBody.create(MediaType.parse("image/jpg"), image)
        val typeOfferResponse = OfferResponse::class.java

        return if (clazz.name == typeOfferResponse.name) {
            val obs1 = imageMatchingService.matchAndGetRequestId(endpoints.imageMatchingUrl, headers, body)
            convertResponseBasedOnType(image, obs1)
        } else {
            val obs1 = imageMatchingService.match(endpoints.imageMatchingUrl, headers, body)
            convertResponseBodyBasedOnType(image, obs1, clazz, gson)
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun <T : IResponse> match(image: FloatArray, clazz: Class<T>): Single<T> {
        if (recommendationOptions.enabled && !ternaryOr(exactOptions.enabled, similarityOptions.enabled, ocrOptions.enabled)) {
            val exception = Exception("To use the recommendation feature, you need to enable one of this stages : exact, similarity.")
            return Single.error<T>(exception)
        }

        if (regroupOptions.enabled && !ternaryOr(exactOptions.enabled, similarityOptions.enabled, ocrOptions.enabled)) {
            val exception = Exception("To use the regrouping feature, you need to enable one of this stages : exact, similarity, ocr.")
            return Single.error<T>(exception)
        }

        val b64 = encodeFloatArray(image)
        val json = "{\"b64\":\"$b64\"}"
        val headers = buildHeaders(json.length)
        val body = RequestBody.create(MediaType.parse("application/json"), json)
        val typeOfferResponse = OfferResponse::class.java

        return if (clazz.name == typeOfferResponse.name) {
            val obs1 = imageMatchingService.matchAndGetRequestId(endpoints.imageMatchingUrl2, headers, body)
            convertResponseBasedOnType(image, obs1)
        } else {
            val obs1 = imageMatchingService.match(endpoints.imageMatchingUrl2, headers, body)
            convertResponseBodyBasedOnType(image, obs1, clazz, gson)
        }
    }
}