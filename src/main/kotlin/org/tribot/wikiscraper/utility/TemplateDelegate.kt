package org.tribot.wikiscraper.utility

import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.ReadOnlyProperty

abstract class TemplateDelegate {

    @JvmOverloads
    inline fun <reified T: Any> parse(string: String, defaultValue: T? = null, noinline defaultParse: ((String) -> T?)? = null): T =
        defaultParse?.invoke(string) ?: string.getNonNullable(defaultValue)

    @JvmOverloads
    inline fun <reified T: Any> parseOrNull(string: String, noinline defaultParse: ((String) -> T?)? = null): T? =
        defaultParse?.invoke(string) ?: string.getNullable()

    @JvmOverloads
    fun int(key: String? = null, default: Int? = null): ReadOnlyProperty<TemplateParsed, Int> =
        ReadOnlyProperty { template, property -> template.getStringValue(key ?: property.name).getNonNullable(default) }

    @JvmOverloads
    fun intOrNull(key: String? = null): ReadOnlyProperty<TemplateParsed, Int?> =
        ReadOnlyProperty { template, property -> template.getStringValue(key ?: property.name).getNullable() }

    @JvmOverloads
    fun long(key: String? = null, default: Long? = null): ReadOnlyProperty<TemplateParsed, Long> =
        ReadOnlyProperty { template, property -> template.getStringValue(key ?: property.name).getNonNullable(default) }

    @JvmOverloads
    fun longOrNull(key: String? = null): ReadOnlyProperty<TemplateParsed, Long?> =
        ReadOnlyProperty { template, property -> template.getStringValue(key ?: property.name).getNullable() }

    @JvmOverloads
    fun double(key: String? = null, default: Double? = null): ReadOnlyProperty<TemplateParsed, Double> =
        ReadOnlyProperty { template, property -> template.getStringValue(key ?: property.name).getNonNullable(default) }

    @JvmOverloads
    fun doubleOrNull(key: String? = null): ReadOnlyProperty<TemplateParsed, Double?> =
        ReadOnlyProperty { template, property -> template.getStringValue(key ?: property.name).getNullable() }

    @JvmOverloads
    fun string(key: String? = null, default: String? = null): ReadOnlyProperty<TemplateParsed, String> =
        ReadOnlyProperty { template, property -> template.getStringValue(key ?: property.name).getNonNullable(default) }

    @JvmOverloads
    fun stringOrNull(key: String? = null): ReadOnlyProperty<TemplateParsed, String?> =
        ReadOnlyProperty { template, property -> template.getStringValue(key ?: property.name).getNullable() }

    @JvmOverloads
    fun stringOrDefault(default: String? = null): ReadOnlyProperty<TemplateParsed, String> =
        ReadOnlyProperty { template, property -> template.getStringValue(property.name).getNonNullable(default) }

    @JvmOverloads
    fun boolean(key: String? = null, default: Boolean? = null): ReadOnlyProperty<TemplateParsed, Boolean> =
        ReadOnlyProperty { template, property -> template.getStringValue(key ?: property.name).getNonNullable(default) }

    @JvmOverloads
    fun booleanOrNull(key: String? = null): ReadOnlyProperty<TemplateParsed, Boolean?> =
        ReadOnlyProperty { template, property -> template.getStringValue(key ?: property.name).getNullable() }

    @JvmOverloads
    fun date(key: String? = null, default: Date? = null, format: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")): ReadOnlyProperty<TemplateParsed, Date> =
        ReadOnlyProperty { template, property -> template.getStringValue(key ?: property.name).getDateNonNullable(default, format) }

    @JvmOverloads
    fun dateOrNull(key: String? = null, format: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")): ReadOnlyProperty<TemplateParsed, Date?> =
        ReadOnlyProperty { template, property -> template.getStringValue(key ?: property.name).getDateNullable(format) }

    @JvmOverloads
    inline fun <reified T : Any> delegate(key: String? = null, defaultValue: T? = null, noinline parse: ((String) -> T?)? = null): ReadOnlyProperty<TemplateParsed, T> =
        ReadOnlyProperty { template, property ->
            val string = template.getStringValue(key ?: property.name)
            runCatching { parse?.invoke(string) }.getOrNull() ?: string.getNonNullable(defaultValue)
        }

    @JvmOverloads
    inline fun <reified T : Any> delegateOrNull(key: String? = null, noinline parse: ((String) -> T?)? = null): ReadOnlyProperty<TemplateParsed, T?> =
        ReadOnlyProperty { template, property ->
            val string = template.getStringValue(key ?: property.name)
            runCatching { parse?.invoke(string) }.getOrNull() ?: string.getNullable()
        }

    @JvmOverloads
    inline fun <reified T : Any> list(
        key: String? = null,
        noinline splitter: ((String) -> List<String>)? = null,
        defaultValue: List<T>? = null
    ): ReadOnlyProperty<TemplateParsed, List<T>> =
        ReadOnlyProperty { template, property ->
            template.getStringValue(key ?: property.name).getList(defaultValue, splitter) ?: emptyList()
        }

    @JvmOverloads
    inline fun <reified T : Any> listOrNull(
        key: String? = null,
        noinline splitter: ((String) -> List<String>)? = null,
    ): ReadOnlyProperty<TemplateParsed, List<T>?> =
        ReadOnlyProperty { template, property ->
            template.getStringValue(key ?: property.name).getList(splitter = splitter)
        }


    inline fun <reified T : Any> Collection<String>.mapNotNull(): List<T> = mapNotNull { it.getNullable() }

    inline fun <reified T : Any> String.getList(
        defaultValue: List<T>? = null,
        noinline splitter: ((String) -> List<String>)? = null
    ): List<T>? {
        if (this.isEmpty()) return defaultValue
        val stringList = runCatching {
            if (splitter != null) splitter(this) else split(',')
        }.getOrNull() ?: return defaultValue
        if (stringList.isEmpty()) return defaultValue
        return stringList.mapNotNull<T>().ifEmpty { defaultValue }
    }
}