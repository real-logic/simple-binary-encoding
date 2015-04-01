using System;
using System.CodeDom.Compiler;
using System.Runtime.InteropServices;
using Microsoft.VisualStudio;
using Microsoft.VisualStudio.OLE.Interop;
using Microsoft.VisualStudio.Shell;
using Microsoft.VisualStudio.Shell.Interop;

namespace Adaptive.SimpleBinaryEncoding.CodeGen
{
    /// <summary>Helper class with boilerplate common to all single-file generators.</summary>
    [ComVisible(true)]
    public abstract class CustomToolBase : IVsSingleFileGenerator, IObjectWithSite
    {
        private object _site;
        private CodeDomProvider _codeDomProvider;
        private ServiceProvider _serviceProvider;

        protected abstract string DefaultExtension();
        public int DefaultExtension(out string defExt)
        {
            return (defExt = DefaultExtension()).Length;
        }

        protected abstract byte[] Generate(string inputFilePath, string inputFileContents, string defaultNamespace, IVsGeneratorProgress progressCallback);

        public virtual int Generate(string inputFilePath, string inputFileContents, string defaultNamespace, IntPtr[] outputFileContents, out uint outputSize, IVsGeneratorProgress progressCallback)
        {
            if (inputFileContents == null)
                throw new ArgumentException("inputFileContents");

            try
            {
                byte[] outputBytes = Generate(inputFilePath, inputFileContents, defaultNamespace, progressCallback);
                if (outputBytes != null)
                {
                    outputSize = (uint)outputBytes.Length;
                    outputFileContents[0] = Marshal.AllocCoTaskMem(outputBytes.Length);
                    Marshal.Copy(outputBytes, 0, outputFileContents[0], outputBytes.Length);
                }
                else
                {
                    outputFileContents[0] = IntPtr.Zero;
                    outputSize = 0;
                }
                return VSConstants.S_OK;
            }
            catch (Exception e)
            {
                // Error msg in Visual Studio only gives the exception message, 
                // not the stack trace. Workaround:
                throw new COMException(string.Format("{0}: {1}\n{2}", e.GetType().Name, e.Message, e.StackTrace));
            }
        }

        #region IObjectWithSite

        public void GetSite(ref Guid riid, out IntPtr ppvSite)
        {
            if (_site == null)
                Marshal.ThrowExceptionForHR(VSConstants.E_NOINTERFACE);

            // Query for the interface using the site object initially passed to the generator 
            IntPtr punk = Marshal.GetIUnknownForObject(_site);
            int hr = Marshal.QueryInterface(punk, ref riid, out ppvSite);
            Marshal.Release(punk);
            Microsoft.VisualStudio.ErrorHandler.ThrowOnFailure(hr);
        }

        public void SetSite(object pUnkSite)
        {
            // Save away the site object for later use 
            _site = pUnkSite;

            // These are initialized on demand via our private CodeProvider and SiteServiceProvider properties 
            _codeDomProvider = null;
            _serviceProvider = null;
        }

        #endregion IObjectWithSite
    }
}
