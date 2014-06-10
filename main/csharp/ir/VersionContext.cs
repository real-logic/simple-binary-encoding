namespace Adaptive.SimpleBinaryEncoding.ir
{
    /// <summary>
    /// Indicates how the version field should be interpreted. 
    /// </summary>
    public enum VersionContext
    {
        /// <summary>
        /// Indicates the version is for the template itself. 
        /// </summary>
        TemplateVersion,

        /// <summary>
        /// Indicates the field was introduced since this template version. 
        /// </summary>
        SinceTemplateVersion
    }
}