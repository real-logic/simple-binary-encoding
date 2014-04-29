using System.Collections.Generic;
using System.Collections.ObjectModel;
using Adaptive.SimpleBinaryEncoding.Util;

namespace Adaptive.SimpleBinaryEncoding.ir
{
    /// <summary>
    /// Intermediate representation of SBE messages to be used for the generation of encoders and decoders
    /// as stubs in various languages.
    /// </summary>
    public class IntermediateRepresentation
    {
        private readonly string _packageName;
        private readonly string _namespaceName;
        private readonly int _id;
        private readonly int _version;
        private readonly string _semanticVersion;
        
        private readonly HeaderStructure _headerStructure;
	    private readonly IDictionary<long, IList<Token>> _messagesByIdMap = new Dictionary<long, IList<Token>>();
	    private readonly IDictionary<string, IList<Token>> _typesByNameMap = new Dictionary<string, IList<Token>>();

        /// <summary>
        /// Create a new IR container taking a defensive copy of the headerStructure <seealso cref="Token"/>s passed.
        /// </summary>
        /// <param name="packageName"> that should be applied to generated code. </param>
        /// <param name="namespaceName"> that should be applied to generated code.</param>
        /// <param name="semanticVersion">semantic version for mapping to the application domain.</param>
        /// <param name="headerTokens"> representing the message headerStructure. </param>
        /// <param name="id"></param>
        /// <param name="version"></param>
        public IntermediateRepresentation(string packageName, 
            string namespaceName, 
            int id,
            int version,
            string semanticVersion,
            IList<Token> headerTokens)
	    {
		    Verify.NotNull(packageName, "packageName");
		    Verify.NotNull(headerTokens, "headerTokens");

		    _packageName = packageName;
            _namespaceName = namespaceName;
            _id = id;
		    _version = version;
            _semanticVersion = semanticVersion;
            _headerStructure = new HeaderStructure(new List<Token>(headerTokens));
        }

        /// <summary>
        /// Return the <seealso cref="HeaderStructure"/> description for all messages.
        /// </summary>
        /// <value>the &lt;seealso cref=&quot;HeaderStructure&quot;/&gt; description for all messages.</value>
        public HeaderStructure HeaderStructure
        {
            get { return _headerStructure; }
        }

        /// <summary>
	    /// Add a List of <seealso cref="Token"/>s for a given message id.
	    /// </summary>
	    /// <param name="messageId"> to identify the list of tokens for the message. </param>
	    /// <param name="messageTokens"> the List of <seealso cref="Token"/>s representing the message. </param>
	    public void AddMessage(long messageId, IList<Token> messageTokens)
	    {
            Verify.NotNull(messageTokens, "messageTokens");

		    CaptureTypes(messageTokens);

		    _messagesByIdMap[messageId] = new List<Token>(messageTokens);
	    }

	    /// <summary>
	    /// Get the getMessage for a given identifier.
	    /// </summary>
	    /// <param name="messageId"> to get. </param>
	    /// <returns> the List of <seealso cref="Token"/>s representing the message or null if the id is not found. </returns>
	    public IList<Token> GetMessage(long messageId)
	    {
		    return _messagesByIdMap[messageId];
	    }

	    /// <summary>
	    /// Get the type representation for a given type name.
	    /// </summary>
	    /// <param name="name"> of type to get. </param>
	    /// <returns> the List of <seealso cref="Token"/>s representing the type or null if the name is not found. </returns>
	    public IList<Token> GetType(string name)
	    {
		    return _typesByNameMap[name];
	    }

        /// <summary>
        /// Get the <seealso cref="Collection{T}"/> of types in for this schema.
        /// </summary>
        /// <value>the collection of types in for this schema.</value>
        public ICollection<IList<Token>> Types
        {
            get { return _typesByNameMap.Values; }
        }

        /// <summary>
        /// The collection of messages in this schema.
        /// </summary>
        /// <value>the collection of messages in this schema.</value>
        public ICollection<IList<Token>> Messages
        {
            get { return _messagesByIdMap.Values; }
        }

        /// <summary>
        /// Get the package name to be used for generated code.
        /// </summary>
        /// <value>the package name to be used for generated code.</value>
        public string PackageName
        {
            get { return _packageName; }
        }

        /// <summary>
        /// Get the namespace name to be used for generated code.
        /// </summary>
        /// <value>the namespace name to be used for generated code.</value>
        public string NamespaceName
        {
            get { return _namespaceName; }
        }

        /// <summary>
        /// Get the id number of the schema.
        /// </summary>
        public int Id
        {
            get { return _id; }
        }

        /// <summary>
        /// Get the namespaceName to be used for generated code.
        ///
        /// If <seealso cref="NamespaceName"/> is null then <seealso cref="PackageName"/> is used.
        /// </summary>
        /// <returns> the namespaceName to be used for generated code. </returns>
        public string ApplicableNamespace
        {
            get { return _namespaceName ?? _packageName; }
        }

        /// <summary>
        /// Get the version of the schema.
        /// </summary>
        /// <value>version number.</value>
        public int Version
        {
            get { return _version; }
        }

        /// <summary>
        /// Get the semantic version of the schema.
        /// </summary>
        public string SemanticVersion
        {
            get { return _semanticVersion; }
        }

        private void CaptureTypes(IList<Token> tokens)
	    {
		    for (int i = 0, size = tokens.Count; i < size; i++)
		    {
			    switch (tokens[i].Signal)
			    {
				    case Signal.BeginComposite:
					    i = CaptureType(tokens, i, Signal.EndComposite);
					    break;

				    case Signal.BeginEnum:
					    i = CaptureType(tokens, i, Signal.EndEnum);
					    break;

                    case Signal.BeginSet:
					    i = CaptureType(tokens, i, Signal.EndSet);
					    break;
			    }
		    }
	    }

	    private int CaptureType(IList<Token> tokens, int index, Signal endSignal)
	    {
		    IList<Token> typeTokens = new List<Token>();

		    Token token = tokens[index];
		    typeTokens.Add(token);
		    do
		    {
			    token = tokens[++index];
			    typeTokens.Add(token);
		    }
		    while (endSignal != token.Signal);

		    _typesByNameMap[tokens[index].Name] = typeTokens;

		    return index;
	    }
    }
}