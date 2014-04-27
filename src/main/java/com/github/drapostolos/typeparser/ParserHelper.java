package com.github.drapostolos.typeparser;

import static com.github.drapostolos.typeparser.TypeParserUtility.getParameterizedTypeArguments;
import static com.github.drapostolos.typeparser.TypeParserUtility.makeNullArgumentErrorMsg;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * Helper class providing helper methods to implementations of {@link Parser} when parsing a
 * string to a type.
 * <p/>
 * The {@link TypeParser} will automatically inject an instance of this class into the
 * {@link Parser} implementation.
 * 
 * @see <a href="https://github.com/drapostolos/type-parser/wiki/User-Guide">User-Guide</a>
 */
public final class ParserHelper {

    private final Type targetType;
    private final TypeParser stringParser;
    private final SplitStrategy splitStrategy;
    private final SplitStrategy keyValueSplitStrategy;

    ParserHelper(TypeParser typeParser, Type targetType) {
        this.stringParser = typeParser;
        this.targetType = targetType;
        this.splitStrategy = typeParser.splitStrategy;
        this.keyValueSplitStrategy = typeParser.keyValueSplitStrategy;
    }

    /**
     * This method gives access to {@link TypeParser#parse(String, Class)}.
     * 
     * @param input String to parse.
     * @param targetType to parse it to.
     * @return an instance of type.
     */
    public <T> T parse(String input, Class<T> targetType) {
        return stringParser.parse(input, targetType);
    }

    /**
     * This method gives access to {@link TypeParser#parseType(String, Type)}.
     * 
     * @param input String to parse.
     * @param targetType The target type to parse the given input to.
     * @return an instance of type.
     */
    public Object parseType(String input, Type targetType) {
        return stringParser.parseType(input, targetType);
    }

    /**
     * Splits the {@code input} string into a list of sub-strings by using the {@link SplitStrategy}
     * implementation, as registered with {@link TypeParserBuilder#setSplitStrategy(SplitStrategy)}.
     * <p/>
     * If {@code input} is null, an empty list is returned, without calling the registered
     * {@link SplitStrategy}.
     * <p/>
     * For example the default {@link SplitStrategy} will split this string "1, 2, 3, 4" into ["1",
     * " 2", " 3", " 4"].
     * <p/>
     * 
     * @param input String to parse. For example "THIS, THAT, OTHER"
     * @return List of strings.
     * @throws IllegalStateException if registered {@link SplitStrategy} implementation
     *         throws exception.
     */
    public List<String> split(String input) {
        if (input == null) {
            return Collections.emptyList();
        }
        try {
            return splitStrategy.split(input, new SplitStrategyHelper(targetType));
        } catch (Throwable t) {
            String message = "Exception thrown from SplitStrategy: %s [%s] with message:  "
                    + "%s. See underlying exception for more information.";
            message = String.format(message, splitStrategy, splitStrategy.getClass(), t.getMessage());
            throw new IllegalStateException(message, t);
        }

    }

    /**
     * Splits the {@code keyValue} string into a list of sub-strings by using the
     * {@link SplitStrategy} implementation, as registered with
     * {@link TypeParserBuilder#setKeyValueSplitStrategy(SplitStrategy)}.
     * <p/>
     * For example the default behavior splits this string "a=AAA=BBB" into ["a", "AAA=BBB"]. Note!
     * The the string is only split by the first occurring of "=", any subsequent "=" are ignored by
     * the {@link SplitStrategy}.
     * 
     * @param keyValue
     * @return A list of string computed by splitting the {@code keyValue} string using the KeyValue
     *         SplitStrategy.
     * @throws IllegalStateException if registered {@link SplitStrategy} implementation
     *         throws exception.
     */
    public List<String> splitKeyValue(String keyValue) {
        if (keyValue == null) {
            throw new NullPointerException(makeNullArgumentErrorMsg("keyValue"));
        }
        try {
            return keyValueSplitStrategy.split(keyValue, new SplitStrategyHelper(targetType));
        } catch (Throwable t) {
            String message = "Exception thrown from SplitStrategy: %s [%s] with message:  "
                    + "%s. See underlying exception for more information.";
            message = String.format(message,
                    keyValueSplitStrategy, keyValueSplitStrategy.getClass(), t.getMessage());
            throw new IllegalStateException(message, t);
        }
    }

    /**
     * Returns the type to parse the input string to.
     * 
     * @return the {@link Type} to parse to.
     */
    public Type getTargetType() {
        return targetType;
    }

    /**
     * When the {@code targetType} is a parameterized type this method
     * returns a list with the type arguments.
     * <p/>
     * All type arguments must be none parameterized types (i.e. nested parameterized types are not
     * allowed), with one exception: {@link Class<?>}. <br/>
     * 
     * @return List of {@link Class} types.
     * @throws IllegalStateException if the {@code targetType} is not a parameterized type.
     * @throws IllegalStateException if any of the parameterized type arguments is of a
     *         parameterized type (with exception of {@link Class}).
     */
    public <T> List<Class<T>> getParameterizedClassArguments() {
        return getParameterizedTypeArguments(targetType);
    }

    /**
     * Convenient method for retrieving elements by index position in the list as returned from
     * method {@link ParserHelper#getParameterizedClassArguments()}
     * 
     * @param index in list of type arguments.
     * @return Type argument.
     * @throws IllegalArgumentException when {@code index} is negative or larger
     *         tan number of elements in list.
     */
    public <T> Class<T> getParameterizedClassArgumentByIndex(int index) {
        if (index < 0) {
            String message = "Argument named 'index' is illegally "
                    + "set to negative value: %s. Must be positive.";
            throw new IllegalArgumentException(String.format(message, index));
        }
        List<Class<T>> list = getParameterizedClassArguments();
        if (index >= list.size()) {
            String message = "Argument named 'index' is illegally "
                    + "set to value: %s. List size is: %s.";
            throw new IllegalArgumentException(String.format(message, index, list.size()));
        }
        return list.get(index);
    }

    public <T> Class<T> getTargetClass() {
        if (targetType instanceof Class) {
            /*
             * The below cast is correct since we know its an instance of Class
             * and T is erased at runtime (due to javas Type erasure).
             */
            @SuppressWarnings("unchecked")
            Class<T> temp = (Class<T>) targetType;
            return temp;
        }
        String message = "%s [%s] cannot be casted to java.lang.Class";
        message = String.format(message, targetType, targetType.getClass());
        throw new IllegalStateException(message);
    }

}
