package com.codecraft.aidoc.enums;

/**
 * Enumerates the supported comment wrappers used when embedding documentation back into code.
 */
public enum CommentFormat {
    /**
     * JSDoc style multi-line block (/* ... *\/).
     */
    JSDOC,

    /**
     * Python triple quote docstring.
     */
    PYTHON_DOCSTRING,

    /**
     * NumPy structured docstring.
     */
    NUMPY,

    /**
     * XML formatted documentation, used for C# and .NET languages.
     */
    XML,

    /**
     * Ruby RDoc style documentation.
     */
    RDOC,

    /**
     * Uses inline single line comments and wraps text accordingly.
     */
    LINE
}
