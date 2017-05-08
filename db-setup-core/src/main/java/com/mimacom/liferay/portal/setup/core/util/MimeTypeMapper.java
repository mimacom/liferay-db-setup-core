package com.mimacom.liferay.portal.setup.core.util;

/*
 * #%L
 * Liferay Portal DB Setup core
 * %%
 * Copyright (C) 2016 - 2017 mimacom ag
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */


import java.util.HashMap;

/**
 * This class is a mapping tool for the mapping between mime types and file
 * extensions. The tool is used as singleton.
 */
public final class MimeTypeMapper {
    /**
     * The mapping between the mime type and the extension.
     */
    private HashMap mimeToExtension;

    /**
     * The map with the mapping between the extension to the mime type.
     */
    private HashMap extensionToMimeType;

    private HashMap extensionToImageName;

    /**
     * Reference to the singleton instance.
     */
    private static MimeTypeMapper instance = null;

    /**
     * Returns the singleton instance of the mime type mapper.
     *
     * @return
     */
    public static MimeTypeMapper getInstance() {

        if (instance == null) {
            instance = new MimeTypeMapper();
        }
        return instance;
    }

    /**
     * Constructor initializes the mime types.
     */
    private MimeTypeMapper() {
        mimeToExtension = new HashMap();
        extensionToMimeType = new HashMap();
        extensionToImageName = new HashMap();
        init();
    }

    /**
     * Returns the file extension for a given mime type. If no mime type exists,
     * the empty string is returned.
     *
     * @param mimeType
     *            The mime type string for which the file extension should be
     *            retrieved.
     * @return Returns the file extension string of the file type.
     */
    public String getExtension(final String mimeType) {
        String extension = "";
        if (this.mimeToExtension.containsKey(mimeType)) {
            extension = (String) mimeToExtension.get(mimeType);
        }
        return extension;
    }

    /**
     * Returns the mime type for a given file extension.
     *
     * @param extension
     *            The extension for which the mime type should be retrieved.
     * @return Returns the mime type of a given extension.
     */
    public String getMimeType(final String extension) {
        String dottedExtension = getDottedExtension(extension);
        String mime = "";
        if (this.extensionToMimeType.containsKey(dottedExtension)) {
            mime = (String) extensionToMimeType.get(dottedExtension);
        }
        return mime;
    }

    private String getDottedExtension(final String extension) {
        if (!extension.startsWith(".")) {
            return "." + extension;
        }
        return extension;
    }

    /**
     * Initializes a mapping between the given mime type and the given
     * extension.
     *
     * @param mime
     *            The mime type string.
     * @param extension
     *            The file extension string.
     */
    private void addEntry(final String extension, final String mime) {
        mimeToExtension.put(mime, extension);
        extensionToMimeType.put(extension, mime);
    }

    /**
     * Initializes the mappings between all known file types/mime types.
     */
    // CHECKSTYLE:OFF
    private void init() {
        // init mime types
        addEntry(".3dm", "x-world/x-3dmf");
        addEntry(".3dmf", "x-world/x-3dmf");
        addEntry(".a", "application/octet-stream");
        addEntry(".aab", "application/x-authorware-bin");
        addEntry(".aam", "application/x-authorware-map");
        addEntry(".aas", "application/x-authorware-seg");
        addEntry(".abc", "text/vnd.abc");
        addEntry(".acgi", "text/html");
        addEntry(".afl", "video/animaflex");
        addEntry(".ai", "application/postscript");
        addEntry(".aif", "audio/aiff");
        addEntry(".aif", "audio/x-aiff");
        addEntry(".aifc", "audio/aiff");
        addEntry(".aifc", "audio/x-aiff");
        addEntry(".aiff", "audio/aiff");
        addEntry(".aiff", "audio/x-aiff");
        addEntry(".aim", "application/x-aim");
        addEntry(".aip", "text/x-audiosoft-intra");
        addEntry(".ani", "application/x-navi-animation");
        addEntry(".aos", "application/x-nokia-9000-communicator-add-on-software");
        addEntry(".aps", "application/mime");
        addEntry(".arc", "application/octet-stream");
        addEntry(".arj", "application/arj");
        addEntry(".arj", "application/octet-stream");
        addEntry(".art", "image/x-jg");
        addEntry(".asf", "video/x-ms-asf");
        addEntry(".asm", "text/x-asm");
        addEntry(".asp", "text/asp");
        addEntry(".asx", "application/x-mplayer2");
        addEntry(".asx", "video/x-ms-asf");
        addEntry(".asx", "video/x-ms-asf-plugin");
        addEntry(".au", "audio/basic");
        addEntry(".au", "audio/x-au");
        addEntry(".avi", "application/x-troff-msvideo");
        addEntry(".avi", "video/avi");
        addEntry(".avi", "video/msvideo");
        addEntry(".avi", "video/x-msvideo");
        addEntry(".avs", "video/avs-video");
        addEntry(".bcpio", "application/x-bcpio");
        addEntry(".bin", "application/mac-binary");
        addEntry(".bin", "application/macbinary");
        addEntry(".bin", "application/octet-stream");
        addEntry(".bin", "application/x-binary");
        addEntry(".bin", "application/x-macbinary");
        addEntry(".bm", "image/bmp");
        addEntry(".bmp", "image/bmp");
        addEntry(".bmp", "image/x-windows-bmp");
        addEntry(".boo", "application/book");
        addEntry(".book", "application/book");
        addEntry(".boz", "application/x-bzip2");
        addEntry(".bsh", "application/x-bsh");
        addEntry(".bz", "application/x-bzip");
        addEntry(".bz2", "application/x-bzip2");
        addEntry(".c", "text/plain");
        addEntry(".c", "text/x-c");
        addEntry(".c++", "text/plain");
        addEntry(".cat", "application/vnd.ms-pki.seccat");
        addEntry(".cc", "text/plain");
        addEntry(".cc", "text/x-c");
        addEntry(".ccad", "application/clariscad");
        addEntry(".cco", "application/x-cocoa");
        addEntry(".cdf", "application/cdf");
        addEntry(".cdf", "application/x-cdf");
        addEntry(".cdf", "application/x-netcdf");
        addEntry(".cer", "application/pkix-cert");
        addEntry(".cer", "application/x-x509-ca-cert");
        addEntry(".cha", "application/x-chat");
        addEntry(".chat", "application/x-chat");
        addEntry(".class", "application/java");
        addEntry(".class", "application/java-byte-code");
        addEntry(".class", "application/x-java-class");
        addEntry(".com", "application/octet-stream");
        addEntry(".com", "text/plain");
        addEntry(".conf", "text/plain");
        addEntry(".cpio", "application/x-cpio");
        addEntry(".cpp", "text/x-c");
        addEntry(".cpt", "application/mac-compactpro");
        addEntry(".cpt", "application/x-compactpro");
        addEntry(".cpt", "application/x-cpt");
        addEntry(".crl", "application/pkcs-crl");
        addEntry(".crl", "application/pkix-crl");
        addEntry(".crt", "application/pkix-cert");
        addEntry(".crt", "application/x-x509-ca-cert");
        addEntry(".crt", "application/x-x509-user-cert");
        addEntry(".csh", "application/x-csh");
        addEntry(".csh", "text/x-script.csh");
        addEntry(".css", "application/x-pointplus");
        addEntry(".css", "text/css");
        addEntry(".csv", "text/csv");
        addEntry(".cxx", "text/plain");
        addEntry(".dcr", "application/x-director");
        addEntry(".deepv", "application/x-deepv");
        addEntry(".def", "text/plain");
        addEntry(".der", "application/x-x509-ca-cert");
        addEntry(".dif", "video/x-dv");
        addEntry(".dir", "application/x-director");
        addEntry(".dl", "video/dl");
        addEntry(".dl", "video/x-dl");
        addEntry(".doc", "application/msword");
        // addEntry(".docx","application/msword");
        addEntry(".dot", "application/msword");
        // addEntry(".dotx","application/msword");
        addEntry(".dp", "application/commonground");
        addEntry(".drw", "application/drafting");
        addEntry(".dump", "application/octet-stream");
        addEntry(".dv", "video/x-dv");
        addEntry(".dvi", "application/x-dvi");
        addEntry(".dwf", "drawing/x-dwf (old)");
        addEntry(".dwf", "model/vnd.dwf");
        addEntry(".dwg", "application/acad");
        addEntry(".dwg", "image/vnd.dwg");
        addEntry(".dwg", "image/x-dwg");
        addEntry(".dxf", "application/dxf");
        addEntry(".dxf", "image/vnd.dwg");
        addEntry(".dxf", "image/x-dwg");
        addEntry(".dxr", "application/x-director");
        addEntry(".el", "text/x-script.elisp");
        addEntry(".elc", "application/x-bytecode.elisp (compiled elisp)");
        addEntry(".elc", "application/x-elc");
        addEntry(".env", "application/x-envoy");
        addEntry(".eps", "application/postscript");
        addEntry(".es", "application/x-esrehber");
        addEntry(".etx", "text/x-setext");
        addEntry(".evy", "application/envoy");
        addEntry(".evy", "application/x-envoy");
        addEntry(".exe", "application/octet-stream");
        addEntry(".f", "text/plain");
        addEntry(".f", "text/x-fortran");
        addEntry(".f77", "text/x-fortran");
        addEntry(".f90", "text/plain");
        addEntry(".f90", "text/x-fortran");
        addEntry(".fdf", "application/vnd.fdf");
        addEntry(".fif", "application/fractals");
        addEntry(".fif", "image/fif");
        addEntry(".fli", "video/fli");
        addEntry(".fli", "video/x-fli");
        addEntry(".flo", "image/florian");
        addEntry(".flx", "text/vnd.fmi.flexstor");
        addEntry(".fmf", "video/x-atomic3d-feature");
        addEntry(".for", "text/plain");
        addEntry(".for", "text/x-fortran");
        addEntry(".fpx", "image/vnd.fpx");
        addEntry(".fpx", "image/vnd.net-fpx");
        addEntry(".frl", "application/freeloader");
        addEntry(".funk", "audio/make");
        addEntry(".g", "text/plain");
        addEntry(".g3", "image/g3fax");
        addEntry(".gif", "image/gif");
        addEntry(".gl", "video/gl");
        addEntry(".gl", "video/x-gl");
        addEntry(".gsd", "audio/x-gsm");
        addEntry(".gsm", "audio/x-gsm");
        addEntry(".gsp", "application/x-gsp");
        addEntry(".gss", "application/x-gss");
        addEntry(".gtar", "application/x-gtar");
        addEntry(".gz", "application/x-compressed");
        addEntry(".gz", "application/x-gzip");
        addEntry(".gzip", "application/x-gzip");
        addEntry(".gzip", "multipart/x-gzip");
        addEntry(".h", "text/plain");
        addEntry(".h", "text/x-h");
        addEntry(".hdf", "application/x-hdf");
        addEntry(".help", "application/x-helpfile");
        addEntry(".hgl", "application/vnd.hp-hpgl");
        addEntry(".hh", "text/plain");
        addEntry(".hh", "text/x-h");
        addEntry(".hlb", "text/x-script");
        addEntry(".hlp", "application/hlp");
        addEntry(".hlp", "application/x-helpfile");
        addEntry(".hlp", "application/x-winhelp");
        addEntry(".hpg", "application/vnd.hp-hpgl");
        addEntry(".hpgl", "application/vnd.hp-hpgl");
        addEntry(".hqx", "application/binhex");
        addEntry(".hqx", "application/binhex4");
        addEntry(".hqx", "application/mac-binhex");
        addEntry(".hqx", "application/mac-binhex40");
        addEntry(".hqx", "application/x-binhex40");
        addEntry(".hqx", "application/x-mac-binhex40");
        addEntry(".hta", "application/hta");
        addEntry(".htc", "text/x-component");
        addEntry(".htm", "text/html");
        addEntry(".html", "text/html");
        addEntry(".htmls", "text/html");
        addEntry(".htt", "text/webviewhtml");
        addEntry(".htx", "text/html");
        addEntry(".ice", "x-conference/x-cooltalk");
        addEntry(".ico", "image/x-icon");
        addEntry(".idc", "text/plain");
        addEntry(".ief", "image/ief");
        addEntry(".iefs", "image/ief");
        addEntry(".iges", "application/iges");
        addEntry(".iges", "model/iges");
        addEntry(".igs", "application/iges");
        addEntry(".igs", "model/iges");
        addEntry(".ima", "application/x-ima");
        addEntry(".imap", "application/x-httpd-imap");
        addEntry(".inf", "application/inf");
        addEntry(".ins", "application/x-internett-signup");
        addEntry(".ip", "application/x-ip2");
        addEntry(".isu", "video/x-isvideo");
        addEntry(".it", "audio/it");
        addEntry(".iv", "application/x-inventor");
        addEntry(".ivr", "i-world/i-vrml");
        addEntry(".ivy", "application/x-livescreen");
        addEntry(".jam", "audio/x-jam");
        addEntry(".jav", "text/plain");
        addEntry(".jav", "text/x-java-source");
        addEntry(".java", "text/plain");
        addEntry(".java", "text/x-java-source");
        addEntry(".jcm", "application/x-java-commerce");
        addEntry(".jfif", "image/jpeg");
        addEntry(".jfif", "image/pjpeg");
        addEntry(".jfif-tbnl", "image/jpeg");
        addEntry(".jpe", "image/jpeg");
        addEntry(".jpe", "image/pjpeg");
        addEntry(".jpeg", "image/jpeg");
        addEntry(".jpeg", "image/pjpeg");
        addEntry(".jpg", "image/jpeg");
        addEntry(".jpg", "image/pjpeg");
        addEntry(".jps", "image/x-jps");
        addEntry(".js", "application/x-javascript");
        addEntry(".jut", "image/jutvision");
        addEntry(".kar", "audio/midi");
        addEntry(".kar", "music/x-karaoke");
        addEntry(".ksh", "application/x-ksh");
        addEntry(".ksh", "text/x-script.ksh");
        addEntry(".la", "audio/nspaudio");
        addEntry(".la", "audio/x-nspaudio");
        addEntry(".lam", "audio/x-liveaudio");
        addEntry(".latex", "application/x-latex");
        addEntry(".lha", "application/lha");
        addEntry(".lha", "application/octet-stream");
        addEntry(".lha", "application/x-lha");
        addEntry(".lhx", "application/octet-stream");
        addEntry(".list", "text/plain");
        addEntry(".lma", "audio/nspaudio");
        addEntry(".lma", "audio/x-nspaudio");
        addEntry(".log", "text/plain");
        addEntry(".lsp", "application/x-lisp");
        addEntry(".lsp", "text/x-script.lisp");
        addEntry(".lst", "text/plain");
        addEntry(".lsx", "text/x-la-asf");
        addEntry(".ltx", "application/x-latex");
        addEntry(".lzh", "application/octet-stream");
        addEntry(".lzh", "application/x-lzh");
        addEntry(".lzx", "application/lzx");
        addEntry(".lzx", "application/octet-stream");
        addEntry(".lzx", "application/x-lzx");
        addEntry(".m", "text/plain");
        addEntry(".m", "text/x-m");
        addEntry(".m1v", "video/mpeg");
        addEntry(".m2a", "audio/mpeg");
        addEntry(".m2v", "video/mpeg");
        addEntry(".m3u", "audio/x-mpequrl");
        addEntry(".man", "application/x-troff-man");
        addEntry(".map", "application/x-navimap");
        addEntry(".mar", "text/plain");
        addEntry(".mbd", "application/mbedlet");
        addEntry(".mc$", "application/x-magic-cap-package-1.0");
        addEntry(".mcd", "application/mcad");
        addEntry(".mcd", "application/x-mathcad");
        addEntry(".mcf", "image/vasa");
        addEntry(".mcf", "text/mcf");
        addEntry(".mcp", "application/netmc");
        addEntry(".me", "application/x-troff-me");
        addEntry(".mht", "message/rfc822");
        addEntry(".mhtml", "message/rfc822");
        addEntry(".mid", "application/x-midi");
        addEntry(".mid", "audio/midi");
        addEntry(".mid", "audio/x-mid");
        addEntry(".mid", "audio/x-midi");
        addEntry(".mid", "music/crescendo");
        addEntry(".mid", "x-music/x-midi");
        addEntry(".midi", "application/x-midi");
        addEntry(".midi", "audio/midi");
        addEntry(".midi", "audio/x-mid");
        addEntry(".midi", "audio/x-midi");
        addEntry(".midi", "music/crescendo");
        addEntry(".midi", "x-music/x-midi");
        addEntry(".mif", "application/x-frame");
        addEntry(".mif", "application/x-mif");
        addEntry(".mime", "message/rfc822");
        addEntry(".mime", "www/mime");
        addEntry(".mjf", "audio/x-vnd.audioexplosion.mjuicemediafile");
        addEntry(".mjpg", "video/x-motion-jpeg");
        addEntry(".mm", "application/base64");
        addEntry(".mm", "application/x-meme");
        addEntry(".mme", "application/base64");
        addEntry(".mod", "audio/mod");
        addEntry(".mod", "audio/x-mod");
        addEntry(".moov", "video/quicktime");
        addEntry(".mov", "video/quicktime");
        addEntry(".movie", "video/x-sgi-movie");
        addEntry(".mp2", "audio/mpeg");
        addEntry(".mp2", "audio/x-mpeg");
        addEntry(".mp2", "video/mpeg");
        addEntry(".mp2", "video/x-mpeg");
        addEntry(".mp2", "video/x-mpeq2a");
        addEntry(".mp3", "audio/mpeg3");
        addEntry(".mp3", "audio/x-mpeg-3");
        addEntry(".mp3", "video/mpeg");
        addEntry(".mp3", "video/x-mpeg");
        addEntry(".mpa", "audio/mpeg");
        addEntry(".mpa", "video/mpeg");
        addEntry(".mpc", "application/x-project");
        addEntry(".mpe", "video/mpeg");
        addEntry(".mpeg", "video/mpeg");
        addEntry(".mpg", "audio/mpeg");
        addEntry(".mpg", "video/mpeg");
        addEntry(".mpga", "audio/mpeg");
        addEntry(".mpp", "application/vnd.ms-project");
        addEntry(".mpt", "application/x-project");
        addEntry(".mpv", "application/x-project");
        addEntry(".mpx", "application/x-project");
        addEntry(".mrc", "application/marc");
        addEntry(".ms", "application/x-troff-ms");
        addEntry(".mv", "video/x-sgi-movie");
        addEntry(".my", "audio/make");
        addEntry(".mzz", "application/x-vnd.audioexplosion.mzz");
        addEntry(".nap", "image/naplps");
        addEntry(".naplps", "image/naplps");
        addEntry(".nc", "application/x-netcdf");
        addEntry(".ncm", "application/vnd.nokia.configuration-message");
        addEntry(".nif", "image/x-niff");
        addEntry(".niff", "image/x-niff");
        addEntry(".nix", "application/x-mix-transfer");
        addEntry(".nsc", "application/x-conference");
        addEntry(".nvd", "application/x-navidoc");
        addEntry(".o", "application/octet-stream");
        addEntry(".oda", "application/oda");
        addEntry(".omc", "application/x-omc");
        addEntry(".omcd", "application/x-omcdatamaker");
        addEntry(".omcr", "application/x-omcregerator");
        addEntry(".p", "text/x-pascal");
        addEntry(".p10", "application/pkcs10");
        addEntry(".p10", "application/x-pkcs10");
        addEntry(".p12", "application/pkcs-12");
        addEntry(".p12", "application/x-pkcs12");
        addEntry(".p7a", "application/x-pkcs7-signature");
        addEntry(".p7c", "application/pkcs7-mime");
        addEntry(".p7c", "application/x-pkcs7-mime");
        addEntry(".p7m", "application/pkcs7-mime");
        addEntry(".p7m", "application/x-pkcs7-mime");
        addEntry(".p7r", "application/x-pkcs7-certreqresp");
        addEntry(".p7s", "application/pkcs7-signature");
        addEntry(".part", "application/pro_eng");
        addEntry(".pas", "text/pascal");
        addEntry(".pbm", "image/x-portable-bitmap");
        addEntry(".pcl", "application/vnd.hp-pcl");
        addEntry(".pcl", "application/x-pcl");
        addEntry(".pct", "image/x-pict");
        addEntry(".pcx", "image/x-pcx");
        addEntry(".pdb", "chemical/x-pdb");
        addEntry(".pdf", "application/pdf");
        addEntry(".pfunk", "audio/make");
        addEntry(".pfunk", "audio/make.my.funk");
        addEntry(".pgm", "image/x-portable-graymap");
        addEntry(".pgm", "image/x-portable-greymap");
        addEntry(".pic", "image/pict");
        addEntry(".pict", "image/pict");
        addEntry(".pkg", "application/x-newton-compatible-pkg");
        addEntry(".pko", "application/vnd.ms-pki.pko");
        addEntry(".pl", "text/plain");
        addEntry(".pl", "text/x-script.perl");
        addEntry(".plx", "application/x-pixclscript");
        addEntry(".pm", "image/x-xpixmap");
        addEntry(".pm", "text/x-script.perl-module");
        addEntry(".pm4", "application/x-pagemaker");
        addEntry(".pm5", "application/x-pagemaker");
        addEntry(".png", "image/png");
        addEntry(".pnm", "application/x-portable-anymap");
        addEntry(".pnm", "image/x-portable-anymap");
        addEntry(".pot", "application/mspowerpoint");
        addEntry(".pot", "application/vnd.ms-powerpoint");
        addEntry(".pov", "model/x-pov");
        addEntry(".ppa", "application/vnd.ms-powerpoint");
        addEntry(".ppm", "image/x-portable-pixmap");
        addEntry(".pps", "application/mspowerpoint");
        addEntry(".pps", "application/vnd.ms-powerpoint");
        addEntry(".ppt", "application/mspowerpoint");
        addEntry(".ppt", "application/powerpoint");
        // addEntry(".pptx","application/powerpoint");
        addEntry(".ppt", "application/vnd.ms-powerpoint");
        addEntry(".ppt", "application/x-mspowerpoint");
        addEntry(".ppz", "application/mspowerpoint");
        addEntry(".pre", "application/x-freelance");
        addEntry(".prt", "application/pro_eng");
        addEntry(".ps", "application/postscript");
        addEntry(".psd", "application/octet-stream");
        addEntry(".pvu", "paleovu/x-pv");
        addEntry(".pwz", "application/vnd.ms-powerpoint");
        addEntry(".py", "text/x-script.phyton");
        addEntry(".pyc", "applicaiton/x-bytecode.python");
        addEntry(".qcp", "audio/vnd.qcelp");
        addEntry(".qd3", "x-world/x-3dmf");
        addEntry(".qd3d", "x-world/x-3dmf");
        addEntry(".qif", "image/x-quicktime");
        addEntry(".qt", "video/quicktime");
        addEntry(".qtc", "video/x-qtc");
        addEntry(".qti", "image/x-quicktime");
        addEntry(".qtif", "image/x-quicktime");
        addEntry(".ra", "audio/x-pn-realaudio");
        addEntry(".ra", "audio/x-pn-realaudio-plugin");
        addEntry(".ra", "audio/x-realaudio");
        addEntry(".ram", "audio/x-pn-realaudio");
        addEntry(".ras", "application/x-cmu-raster");
        addEntry(".ras", "image/cmu-raster");
        addEntry(".ras", "image/x-cmu-raster");
        addEntry(".rast", "image/cmu-raster");
        addEntry(".rexx", "text/x-script.rexx");
        addEntry(".rf", "image/vnd.rn-realflash");
        addEntry(".rgb", "image/x-rgb");
        addEntry(".rm", "application/vnd.rn-realmedia");
        addEntry(".rm", "audio/x-pn-realaudio");
        addEntry(".rmi", "audio/mid");
        addEntry(".rmm", "audio/x-pn-realaudio");
        addEntry(".rmp", "audio/x-pn-realaudio");
        addEntry(".rmp", "audio/x-pn-realaudio-plugin");
        addEntry(".rng", "application/ringing-tones");
        addEntry(".rng", "application/vnd.nokia.ringing-tone");
        addEntry(".rnx", "application/vnd.rn-realplayer");
        addEntry(".roff", "application/x-troff");
        addEntry(".rp", "image/vnd.rn-realpix");
        addEntry(".rpm", "audio/x-pn-realaudio-plugin");
        addEntry(".rt", "text/richtext");
        addEntry(".rt", "text/vnd.rn-realtext");
        addEntry(".rtf", "application/rtf");
        addEntry(".rtf", "application/x-rtf");
        addEntry(".rtf", "text/richtext");
        addEntry(".rtx", "application/rtf");
        addEntry(".rtx", "text/richtext");
        addEntry(".rv", "video/vnd.rn-realvideo");
        addEntry(".s", "text/x-asm");
        addEntry(".s3m", "audio/s3m");
        addEntry(".saveme", "application/octet-stream");
        addEntry(".sbk", "application/x-tbook");
        addEntry(".scm", "application/x-lotusscreencam");
        addEntry(".scm", "text/x-script.guile");
        addEntry(".scm", "text/x-script.scheme");
        addEntry(".scm", "video/x-scm");
        addEntry(".sdml", "text/plain");
        addEntry(".sdp", "application/sdp");
        addEntry(".sdp", "application/x-sdp");
        addEntry(".sdr", "application/sounder");
        addEntry(".sea", "application/sea");
        addEntry(".sea", "application/x-sea");
        addEntry(".set", "application/set");
        addEntry(".sgm", "text/sgml");
        addEntry(".sgm", "text/x-sgml");
        addEntry(".sgml", "text/sgml");
        addEntry(".sgml", "text/x-sgml");
        addEntry(".sh", "application/x-bsh");
        addEntry(".sh", "application/x-sh");
        addEntry(".sh", "application/x-shar");
        addEntry(".sh", "text/x-script.sh");
        addEntry(".shar", "application/x-bsh");
        addEntry(".shar", "application/x-shar");
        addEntry(".shtml", "text/html");
        addEntry(".shtml", "text/x-server-parsed-html");
        addEntry(".sid", "audio/x-psid");
        addEntry(".sit", "application/x-sit");
        addEntry(".sit", "application/x-stuffit");
        addEntry(".skd", "application/x-koan");
        addEntry(".skm", "application/x-koan");
        addEntry(".skp", "application/x-koan");
        addEntry(".skt", "application/x-koan");
        addEntry(".sl", "application/x-seelogo");
        addEntry(".smi", "application/smil");
        addEntry(".smil", "application/smil");
        addEntry(".snd", "audio/basic");
        addEntry(".snd", "audio/x-adpcm");
        addEntry(".sol", "application/solids");
        addEntry(".spc", "application/x-pkcs7-certificates");
        addEntry(".spc", "text/x-speech");
        addEntry(".spl", "application/futuresplash");
        addEntry(".spr", "application/x-sprite");
        addEntry(".sprite", "application/x-sprite");
        addEntry(".src", "application/x-wais-source");
        addEntry(".ssi", "text/x-server-parsed-html");
        addEntry(".ssm", "application/streamingmedia");
        addEntry(".sst", "application/vnd.ms-pki.certstore");
        addEntry(".step", "application/step");
        addEntry(".stl", "application/sla");
        addEntry(".stl", "application/vnd.ms-pki.stl");
        addEntry(".stl", "application/x-navistyle");
        addEntry(".stp", "application/step");
        addEntry(".sv4cpio", "application/x-sv4cpio");
        addEntry(".sv4crc", "application/x-sv4crc");
        addEntry(".svf", "image/vnd.dwg");
        addEntry(".svf", "image/x-dwg");
        addEntry(".svr", "application/x-world");
        addEntry(".svr", "x-world/x-svr");
        addEntry(".swf", "application/x-shockwave-flash");
        addEntry(".t", "application/x-troff");
        addEntry(".talk", "text/x-speech");
        addEntry(".tar", "application/x-tar");
        addEntry(".tbk", "application/toolbook");
        addEntry(".tbk", "application/x-tbook");
        addEntry(".tcl", "application/x-tcl");
        addEntry(".tcl", "text/x-script.tcl");
        addEntry(".tcsh", "text/x-script.tcsh");
        addEntry(".tex", "application/x-tex");
        addEntry(".texi", "application/x-texinfo");
        addEntry(".texinfo", "application/x-texinfo");
        addEntry(".text", "application/plain");
        addEntry(".text", "text/plain");
        addEntry(".tgz", "application/gnutar");
        addEntry(".tgz", "application/x-compressed");
        addEntry(".tif", "image/tiff");
        addEntry(".tif", "image/x-tiff");
        addEntry(".tiff", "image/tiff");
        addEntry(".tiff", "image/x-tiff");
        addEntry(".tr", "application/x-troff");
        addEntry(".tsi", "audio/tsp-audio");
        addEntry(".tsp", "application/dsptype");
        addEntry(".tsp", "audio/tsplayer");
        addEntry(".tsv", "text/tab-separated-values");
        addEntry(".turbot", "image/florian");
        addEntry(".txt", "text/plain");
        addEntry(".uil", "text/x-uil");
        addEntry(".uni", "text/uri-list");
        addEntry(".unis", "text/uri-list");
        addEntry(".unv", "application/i-deas");
        addEntry(".uri", "text/uri-list");
        addEntry(".uris", "text/uri-list");
        addEntry(".ustar", "application/x-ustar");
        addEntry(".ustar", "multipart/x-ustar");
        addEntry(".uu", "application/octet-stream");
        addEntry(".uu", "text/x-uuencode");
        addEntry(".uue", "text/x-uuencode");
        addEntry(".vcd", "application/x-cdlink");
        addEntry(".vcs", "text/x-vcalendar");
        addEntry(".vda", "application/vda");
        addEntry(".vdo", "video/vdo");
        addEntry(".vew", "application/groupwise");
        addEntry(".viv", "video/vivo");
        addEntry(".viv", "video/vnd.vivo");
        addEntry(".vivo", "video/vivo");
        addEntry(".vivo", "video/vnd.vivo");
        addEntry(".vmd", "application/vocaltec-media-desc");
        addEntry(".vmf", "application/vocaltec-media-file");
        addEntry(".voc", "audio/voc");
        addEntry(".voc", "audio/x-voc");
        addEntry(".vos", "video/vosaic");
        addEntry(".vox", "audio/voxware");
        addEntry(".vqe", "audio/x-twinvq-plugin");
        addEntry(".vqf", "audio/x-twinvq");
        addEntry(".vql", "audio/x-twinvq-plugin");
        addEntry(".vrml", "application/x-vrml");
        addEntry(".vrml", "model/vrml");
        addEntry(".vrml", "x-world/x-vrml");
        addEntry(".vrt", "x-world/x-vrt");
        addEntry(".vsd", "application/x-visio");
        addEntry(".vst", "application/x-visio");
        addEntry(".vsw", "application/x-visio");
        addEntry(".w60", "application/wordperfect6.0");
        addEntry(".w61", "application/wordperfect6.1");
        addEntry(".w6w", "application/msword");
        addEntry(".wav", "audio/wav");
        addEntry(".wav", "audio/x-wav");
        addEntry(".wb1", "application/x-qpro");
        addEntry(".wbmp", "image/vnd.wap.wbmp");
        addEntry(".web", "application/vnd.xara");
        addEntry(".wiz", "application/msword");
        addEntry(".wk1", "application/x-123");
        addEntry(".wmf", "windows/metafile");
        addEntry(".wml", "text/vnd.wap.wml");
        addEntry(".wmlc", "application/vnd.wap.wmlc");
        addEntry(".wmls", "text/vnd.wap.wmlscript");
        addEntry(".wmlsc", "application/vnd.wap.wmlscriptc");
        addEntry(".word", "application/msword");
        addEntry(".wp", "application/wordperfect");
        addEntry(".wp5", "application/wordperfect");
        addEntry(".wp5", "application/wordperfect6.0");
        addEntry(".wp6", "application/wordperfect");
        addEntry(".wpd", "application/wordperfect");
        addEntry(".wpd", "application/x-wpwin");
        addEntry(".wq1", "application/x-lotus");
        addEntry(".wri", "application/mswrite");
        addEntry(".wri", "application/x-wri");
        addEntry(".wrl", "application/x-world");
        addEntry(".wrl", "model/vrml");
        addEntry(".wrl", "x-world/x-vrml");
        addEntry(".wrz", "model/vrml");
        addEntry(".wrz", "x-world/x-vrml");
        addEntry(".wsc", "text/scriplet");
        addEntry(".wsrc", "application/x-wais-source");
        addEntry(".wtk", "application/x-wintalk");
        addEntry(".xbm", "image/x-xbitmap");
        addEntry(".xbm", "image/x-xbm");
        addEntry(".xbm", "image/xbm");
        addEntry(".xdr", "video/x-amt-demorun");
        addEntry(".xgz", "xgl/drawing");
        addEntry(".xif", "image/vnd.xiff");
        addEntry(".xl", "application/excel");
        addEntry(".xla", "application/excel");
        addEntry(".xla", "application/x-excel");
        addEntry(".xla", "application/x-msexcel");
        addEntry(".xlb", "application/excel");
        addEntry(".xlb", "application/vnd.ms-excel");
        addEntry(".xlb", "application/x-excel");
        addEntry(".xlc", "application/excel");
        addEntry(".xlc", "application/vnd.ms-excel");
        addEntry(".xlc", "application/x-excel");
        addEntry(".xld", "application/excel");
        addEntry(".xld", "application/x-excel");
        addEntry(".xlk", "application/excel");
        addEntry(".xlk", "application/x-excel");
        addEntry(".xll", "application/excel");
        addEntry(".xll", "application/vnd.ms-excel");
        addEntry(".xll", "application/x-excel");
        addEntry(".xlm", "application/excel");
        addEntry(".xlm", "application/vnd.ms-excel");
        addEntry(".xlm", "application/x-excel");
        addEntry(".xls", "application/vnd.ms-excel");
        addEntry(".xls", "application/x-excel");
        addEntry(".xls", "application/x-msexcel");
        addEntry(".xls", "application/excel");
        // addEntry(".xlsx","application/excel");
        addEntry(".xlt", "application/excel");
        addEntry(".xlt", "application/x-excel");
        addEntry(".xlv", "application/excel");
        addEntry(".xlv", "application/x-excel");
        addEntry(".xlw", "application/excel");
        addEntry(".xlw", "application/vnd.ms-excel");
        addEntry(".xlw", "application/x-excel");
        addEntry(".xlw", "application/x-msexcel");
        addEntry(".xm", "audio/xm");
        addEntry(".xml", "application/xml");
        addEntry(".xml", "text/xml");
        addEntry(".xmz", "xgl/movie");
        addEntry(".xpix", "application/x-vnd.ls-xpix");
        addEntry(".xpm", "image/x-xpixmap");
        addEntry(".xpm", "image/xpm");
        addEntry(".x-png", "image/png");
        addEntry(".xsr", "video/x-amt-showrun");
        addEntry(".xwd", "image/x-xwd");
        addEntry(".xwd", "image/x-xwindowdump");
        addEntry(".xyz", "chemical/x-pdb");
        addEntry(".z", "application/x-compress");
        addEntry(".z", "application/x-compressed");
        addEntry(".zip", "application/x-compressed");
        addEntry(".zip", "application/x-zip-compressed");
        addEntry(".zip", "application/zip");
        addEntry(".zip", "multipart/x-zip");
        addEntry(".zoo", "application/octet-stream");
        addEntry(".zsh", "text/x-script.zsh");

        addEntry(".docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml" + ".document");
        addEntry(".dotx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml" + ".template");
        addEntry(".docm", "application/vnd.ms-word.document.macroEnabled.12");
        addEntry(".dotm", "application/vnd.ms-word.template.macroEnabled.12");
        addEntry(".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        addEntry(".xltx", "application/vnd.openxmlformats-officedocument.spreadsheetml.template");
        addEntry(".xlsm", "application/vnd.ms-excel.sheet.macroEnabled.12");
        addEntry(".xltm", "application/vnd.ms-excel.template.macroEnabled.12");
        addEntry(".xlam", "application/vnd.ms-excel.addin.macroEnabled.12");
        addEntry(".xlsb", "application/vnd.ms-excel.sheet.binary.macroEnabled.12");
        addEntry(".pptx",
                "application/vnd.openxmlformats-officedocument.presentationml" + ".presentation");
        addEntry(".potx", "application/vnd.openxmlformats-officedocument.presentationml.template");
        addEntry(".ppsx", "application/vnd.openxmlformats-officedocument.presentationml.slideshow");
        addEntry(".ppam", "application/vnd.ms-powerpoint.addin.macroEnabled.12");
        addEntry(".pptm", "application/vnd.ms-powerpoint.presentation.macroEnabled.12");
        addEntry(".potm", "application/vnd.ms-powerpoint.template.macroEnabled.12");
        addEntry(".ppsm", "application/vnd.ms-powerpoint.slideshow.macroEnabled.12");

        /*
         * .doc application/msword .dot application/msword
         * 
         * .xls application/vnd.ms-excel .xlt application/vnd.ms-excel .xla
         * application/vnd.ms-excel . .ppt application/vnd.ms-powerpoint .pot
         * application/vnd.ms-powerpoint .pps application/vnd.ms-powerpoint .ppa
         * application/vnd.ms-powerpoint
         */

    }
    // CHECKSTYLE:ON

}
