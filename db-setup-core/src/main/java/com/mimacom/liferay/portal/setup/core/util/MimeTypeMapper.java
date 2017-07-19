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
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    public static final String X_WORLD_X_3DMF = "x-world/x-3dmf";
    public static final String AUDIO_AIFF = "audio/aiff";
    public static final String TEXT_HTML = "text/html";
    public static final String APPLICATION_POSTSCRIPT = "application/postscript";
    public static final String AUDIO_X_AIFF = "audio/x-aiff";
    public static final String TEXT_PLAIN = "text/plain";
    public static final String APPLICATION_VND_MS_POWERPOINT = "application/vnd.ms-powerpoint";
    public static final String APPLICATION_EXCEL = "application/excel";
    public static final String APPLICATION_X_EXCEL = "application/x-excel";
    public static final String APPLICATION_WORDPERFECT = "application/wordperfect";
    public static final String VIDEO_MPEG = "video/mpeg";
    public static final String AUDIO_MPEG = "audio/mpeg";
    public static final String APPLICATION_VND_MS_EXCEL = "application/vnd.ms-excel";
    public static final String MIDI = ".midi";
    public static final String VIDEO_QUICKTIME = "video/quicktime";
    public static final String APPLICATION_X_PROJECT = "application/x-project";
    public static final String APPLICATION_X_COMPRESSED = "application/x-compressed";
    public static final String TEXT_X_FORTRAN = "text/x-fortran";
    public static final String IMAGE_X_DWG = "image/x-dwg";
    public static final String IMAGE_VND_DWG = "image/vnd.dwg";
    public static final String APPLICATION_MSWORD = "application/msword";
    public static final String APPLICATION_X_BSH = "application/x-bsh";
    public static final String TEXT_X_C = "text/x-c";
    public static final String APPLICATION_X_X509_CA_CERT = "application/x-x509-ca-cert";
    /**
     * The mapping between the mime type and the extension.
     */
    private HashMap mimeToExtension;

    /**
     * The map with the mapping between the extension to the mime type.
     */
    private HashMap extensionToMimeType;

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
    private void init() {
        // init mime types
        addEntry(".3dm", X_WORLD_X_3DMF);
        addEntry(".3dmf", X_WORLD_X_3DMF);
        addEntry(".a", APPLICATION_OCTET_STREAM);
        addEntry(".aab", "application/x-authorware-bin");
        addEntry(".aam", "application/x-authorware-map");
        addEntry(".aas", "application/x-authorware-seg");
        addEntry(".abc", "text/vnd.abc");
        addEntry(".acgi", TEXT_HTML);
        addEntry(".afl", "video/animaflex");
        addEntry(".ai", APPLICATION_POSTSCRIPT);
        addEntry(".aif", AUDIO_AIFF);
        addEntry(".aif", AUDIO_X_AIFF);
        addEntry(".aifc", AUDIO_AIFF);
        addEntry(".aifc", AUDIO_X_AIFF);
        addEntry(".aiff", AUDIO_AIFF);
        addEntry(".aiff", AUDIO_X_AIFF);
        addEntry(".aim", "application/x-aim");
        addEntry(".aip", "text/x-audiosoft-intra");
        addEntry(".ani", "application/x-navi-animation");
        addEntry(".aos", "application/x-nokia-9000-communicator-add-on-software");
        addEntry(".aps", "application/mime");
        addEntry(".arc", APPLICATION_OCTET_STREAM);
        addEntry(".arj", "application/arj");
        addEntry(".arj", APPLICATION_OCTET_STREAM);
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
        addEntry(".bin", APPLICATION_OCTET_STREAM);
        addEntry(".bin", "application/x-binary");
        addEntry(".bin", "application/x-macbinary");
        addEntry(".bm", "image/bmp");
        addEntry(".bmp", "image/bmp");
        addEntry(".bmp", "image/x-windows-bmp");
        addEntry(".boo", "application/book");
        addEntry(".book", "application/book");
        addEntry(".boz", "application/x-bzip2");
        addEntry(".bsh", APPLICATION_X_BSH);
        addEntry(".bz", "application/x-bzip");
        addEntry(".bz2", "application/x-bzip2");
        addEntry(".c", TEXT_PLAIN);
        addEntry(".c", TEXT_X_C);
        addEntry(".c++", TEXT_PLAIN);
        addEntry(".cat", "application/vnd.ms-pki.seccat");
        addEntry(".cc", TEXT_PLAIN);
        addEntry(".cc", TEXT_X_C);
        addEntry(".ccad", "application/clariscad");
        addEntry(".cco", "application/x-cocoa");
        addEntry(".cdf", "application/cdf");
        addEntry(".cdf", "application/x-cdf");
        addEntry(".cdf", "application/x-netcdf");
        addEntry(".cer", "application/pkix-cert");
        addEntry(".cer", APPLICATION_X_X509_CA_CERT);
        addEntry(".cha", "application/x-chat");
        addEntry(".chat", "application/x-chat");
        addEntry(".class", "application/java");
        addEntry(".class", "application/java-byte-code");
        addEntry(".class", "application/x-java-class");
        addEntry(".com", APPLICATION_OCTET_STREAM);
        addEntry(".com", TEXT_PLAIN);
        addEntry(".conf", TEXT_PLAIN);
        addEntry(".cpio", "application/x-cpio");
        addEntry(".cpp", TEXT_X_C);
        addEntry(".cpt", "application/mac-compactpro");
        addEntry(".cpt", "application/x-compactpro");
        addEntry(".cpt", "application/x-cpt");
        addEntry(".crl", "application/pkcs-crl");
        addEntry(".crl", "application/pkix-crl");
        addEntry(".crt", "application/pkix-cert");
        addEntry(".crt", APPLICATION_X_X509_CA_CERT);
        addEntry(".crt", "application/x-x509-user-cert");
        addEntry(".csh", "application/x-csh");
        addEntry(".csh", "text/x-script.csh");
        addEntry(".css", "application/x-pointplus");
        addEntry(".css", "text/css");
        addEntry(".csv", "text/csv");
        addEntry(".cxx", TEXT_PLAIN);
        addEntry(".dcr", "application/x-director");
        addEntry(".deepv", "application/x-deepv");
        addEntry(".def", TEXT_PLAIN);
        addEntry(".der", APPLICATION_X_X509_CA_CERT);
        addEntry(".dif", "video/x-dv");
        addEntry(".dir", "application/x-director");
        addEntry(".dl", "video/dl");
        addEntry(".dl", "video/x-dl");
        addEntry(".doc", APPLICATION_MSWORD);
        // addEntry(".docx","application/msword");
        addEntry(".dot", APPLICATION_MSWORD);
        // addEntry(".dotx","application/msword");
        addEntry(".dp", "application/commonground");
        addEntry(".drw", "application/drafting");
        addEntry(".dump", APPLICATION_OCTET_STREAM);
        addEntry(".dv", "video/x-dv");
        addEntry(".dvi", "application/x-dvi");
        addEntry(".dwf", "drawing/x-dwf (old)");
        addEntry(".dwf", "model/vnd.dwf");
        addEntry(".dwg", "application/acad");
        addEntry(".dwg", IMAGE_VND_DWG);
        addEntry(".dwg", IMAGE_X_DWG);
        addEntry(".dxf", "application/dxf");
        addEntry(".dxf", IMAGE_VND_DWG);
        addEntry(".dxf", IMAGE_X_DWG);
        addEntry(".dxr", "application/x-director");
        addEntry(".el", "text/x-script.elisp");
        addEntry(".elc", "application/x-bytecode.elisp (compiled elisp)");
        addEntry(".elc", "application/x-elc");
        addEntry(".env", "application/x-envoy");
        addEntry(".eps", APPLICATION_POSTSCRIPT);
        addEntry(".es", "application/x-esrehber");
        addEntry(".etx", "text/x-setext");
        addEntry(".evy", "application/envoy");
        addEntry(".evy", "application/x-envoy");
        addEntry(".exe", APPLICATION_OCTET_STREAM);
        addEntry(".f", TEXT_PLAIN);
        addEntry(".f", TEXT_X_FORTRAN);
        addEntry(".f77", TEXT_X_FORTRAN);
        addEntry(".f90", TEXT_PLAIN);
        addEntry(".f90", TEXT_X_FORTRAN);
        addEntry(".fdf", "application/vnd.fdf");
        addEntry(".fif", "application/fractals");
        addEntry(".fif", "image/fif");
        addEntry(".fli", "video/fli");
        addEntry(".fli", "video/x-fli");
        addEntry(".flo", "image/florian");
        addEntry(".flx", "text/vnd.fmi.flexstor");
        addEntry(".fmf", "video/x-atomic3d-feature");
        addEntry(".for", TEXT_PLAIN);
        addEntry(".for", TEXT_X_FORTRAN);
        addEntry(".fpx", "image/vnd.fpx");
        addEntry(".fpx", "image/vnd.net-fpx");
        addEntry(".frl", "application/freeloader");
        addEntry(".funk", "audio/make");
        addEntry(".g", TEXT_PLAIN);
        addEntry(".g3", "image/g3fax");
        addEntry(".gif", "image/gif");
        addEntry(".gl", "video/gl");
        addEntry(".gl", "video/x-gl");
        addEntry(".gsd", "audio/x-gsm");
        addEntry(".gsm", "audio/x-gsm");
        addEntry(".gsp", "application/x-gsp");
        addEntry(".gss", "application/x-gss");
        addEntry(".gtar", "application/x-gtar");
        addEntry(".gz", APPLICATION_X_COMPRESSED);
        addEntry(".gz", "application/x-gzip");
        addEntry(".gzip", "application/x-gzip");
        addEntry(".gzip", "multipart/x-gzip");
        addEntry(".h", TEXT_PLAIN);
        addEntry(".h", "text/x-h");
        addEntry(".hdf", "application/x-hdf");
        addEntry(".help", "application/x-helpfile");
        addEntry(".hgl", "application/vnd.hp-hpgl");
        addEntry(".hh", TEXT_PLAIN);
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
        addEntry(".htm", TEXT_HTML);
        addEntry(".html", TEXT_HTML);
        addEntry(".htmls", TEXT_HTML);
        addEntry(".htt", "text/webviewhtml");
        addEntry(".htx", TEXT_HTML);
        addEntry(".ice", "x-conference/x-cooltalk");
        addEntry(".ico", "image/x-icon");
        addEntry(".idc", TEXT_PLAIN);
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
        addEntry(".jav", TEXT_PLAIN);
        addEntry(".jav", "text/x-java-source");
        addEntry(".java", TEXT_PLAIN);
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
        addEntry(".lha", APPLICATION_OCTET_STREAM);
        addEntry(".lha", "application/x-lha");
        addEntry(".lhx", APPLICATION_OCTET_STREAM);
        addEntry(".list", TEXT_PLAIN);
        addEntry(".lma", "audio/nspaudio");
        addEntry(".lma", "audio/x-nspaudio");
        addEntry(".log", TEXT_PLAIN);
        addEntry(".lsp", "application/x-lisp");
        addEntry(".lsp", "text/x-script.lisp");
        addEntry(".lst", TEXT_PLAIN);
        addEntry(".lsx", "text/x-la-asf");
        addEntry(".ltx", "application/x-latex");
        addEntry(".lzh", APPLICATION_OCTET_STREAM);
        addEntry(".lzh", "application/x-lzh");
        addEntry(".lzx", "application/lzx");
        addEntry(".lzx", APPLICATION_OCTET_STREAM);
        addEntry(".lzx", "application/x-lzx");
        addEntry(".m", TEXT_PLAIN);
        addEntry(".m", "text/x-m");
        addEntry(".m1v", VIDEO_MPEG);
        addEntry(".m2a", AUDIO_MPEG);
        addEntry(".m2v", VIDEO_MPEG);
        addEntry(".m3u", "audio/x-mpequrl");
        addEntry(".man", "application/x-troff-man");
        addEntry(".map", "application/x-navimap");
        addEntry(".mar", TEXT_PLAIN);
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
        addEntry(MIDI, "application/x-midi");
        addEntry(MIDI, "audio/midi");
        addEntry(MIDI, "audio/x-mid");
        addEntry(MIDI, "audio/x-midi");
        addEntry(MIDI, "music/crescendo");
        addEntry(MIDI, "x-music/x-midi");
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
        addEntry(".moov", VIDEO_QUICKTIME);
        addEntry(".mov", VIDEO_QUICKTIME);
        addEntry(".movie", "video/x-sgi-movie");
        addEntry(".mp2", AUDIO_MPEG);
        addEntry(".mp2", "audio/x-mpeg");
        addEntry(".mp2", VIDEO_MPEG);
        addEntry(".mp2", "video/x-mpeg");
        addEntry(".mp2", "video/x-mpeq2a");
        addEntry(".mp3", "audio/mpeg3");
        addEntry(".mp3", "audio/x-mpeg-3");
        addEntry(".mp3", VIDEO_MPEG);
        addEntry(".mp3", "video/x-mpeg");
        addEntry(".mpa", AUDIO_MPEG);
        addEntry(".mpa", VIDEO_MPEG);
        addEntry(".mpc", APPLICATION_X_PROJECT);
        addEntry(".mpe", VIDEO_MPEG);
        addEntry(".mpeg", VIDEO_MPEG);
        addEntry(".mpg", AUDIO_MPEG);
        addEntry(".mpg", VIDEO_MPEG);
        addEntry(".mpga", AUDIO_MPEG);
        addEntry(".mpp", "application/vnd.ms-project");
        addEntry(".mpt", APPLICATION_X_PROJECT);
        addEntry(".mpv", APPLICATION_X_PROJECT);
        addEntry(".mpx", APPLICATION_X_PROJECT);
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
        addEntry(".o", APPLICATION_OCTET_STREAM);
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
        addEntry(".pl", TEXT_PLAIN);
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
        addEntry(".pot", APPLICATION_VND_MS_POWERPOINT);
        addEntry(".pov", "model/x-pov");
        addEntry(".ppa", APPLICATION_VND_MS_POWERPOINT);
        addEntry(".ppm", "image/x-portable-pixmap");
        addEntry(".pps", "application/mspowerpoint");
        addEntry(".pps", APPLICATION_VND_MS_POWERPOINT);
        addEntry(".ppt", "application/mspowerpoint");
        addEntry(".ppt", "application/powerpoint");
        // addEntry(".pptx","application/powerpoint");
        addEntry(".ppt", APPLICATION_VND_MS_POWERPOINT);
        addEntry(".ppt", "application/x-mspowerpoint");
        addEntry(".ppz", "application/mspowerpoint");
        addEntry(".pre", "application/x-freelance");
        addEntry(".prt", "application/pro_eng");
        addEntry(".ps", APPLICATION_POSTSCRIPT);
        addEntry(".psd", APPLICATION_OCTET_STREAM);
        addEntry(".pvu", "paleovu/x-pv");
        addEntry(".pwz", APPLICATION_VND_MS_POWERPOINT);
        addEntry(".py", "text/x-script.phyton");
        addEntry(".pyc", "applicaiton/x-bytecode.python");
        addEntry(".qcp", "audio/vnd.qcelp");
        addEntry(".qd3", X_WORLD_X_3DMF);
        addEntry(".qd3d", X_WORLD_X_3DMF);
        addEntry(".qif", "image/x-quicktime");
        addEntry(".qt", VIDEO_QUICKTIME);
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
        addEntry(".saveme", APPLICATION_OCTET_STREAM);
        addEntry(".sbk", "application/x-tbook");
        addEntry(".scm", "application/x-lotusscreencam");
        addEntry(".scm", "text/x-script.guile");
        addEntry(".scm", "text/x-script.scheme");
        addEntry(".scm", "video/x-scm");
        addEntry(".sdml", TEXT_PLAIN);
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
        addEntry(".sh", APPLICATION_X_BSH);
        addEntry(".sh", "application/x-sh");
        addEntry(".sh", "application/x-shar");
        addEntry(".sh", "text/x-script.sh");
        addEntry(".shar", APPLICATION_X_BSH);
        addEntry(".shar", "application/x-shar");
        addEntry(".shtml", TEXT_HTML);
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
        addEntry(".svf", IMAGE_VND_DWG);
        addEntry(".svf", IMAGE_X_DWG);
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
        addEntry(".text", TEXT_PLAIN);
        addEntry(".tgz", "application/gnutar");
        addEntry(".tgz", APPLICATION_X_COMPRESSED);
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
        addEntry(".txt", TEXT_PLAIN);
        addEntry(".uil", "text/x-uil");
        addEntry(".uni", "text/uri-list");
        addEntry(".unis", "text/uri-list");
        addEntry(".unv", "application/i-deas");
        addEntry(".uri", "text/uri-list");
        addEntry(".uris", "text/uri-list");
        addEntry(".ustar", "application/x-ustar");
        addEntry(".ustar", "multipart/x-ustar");
        addEntry(".uu", APPLICATION_OCTET_STREAM);
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
        addEntry(".w6w", APPLICATION_MSWORD);
        addEntry(".wav", "audio/wav");
        addEntry(".wav", "audio/x-wav");
        addEntry(".wb1", "application/x-qpro");
        addEntry(".wbmp", "image/vnd.wap.wbmp");
        addEntry(".web", "application/vnd.xara");
        addEntry(".wiz", APPLICATION_MSWORD);
        addEntry(".wk1", "application/x-123");
        addEntry(".wmf", "windows/metafile");
        addEntry(".wml", "text/vnd.wap.wml");
        addEntry(".wmlc", "application/vnd.wap.wmlc");
        addEntry(".wmls", "text/vnd.wap.wmlscript");
        addEntry(".wmlsc", "application/vnd.wap.wmlscriptc");
        addEntry(".word", APPLICATION_MSWORD);
        addEntry(".wp", APPLICATION_WORDPERFECT);
        addEntry(".wp5", APPLICATION_WORDPERFECT);
        addEntry(".wp5", "application/wordperfect6.0");
        addEntry(".wp6", APPLICATION_WORDPERFECT);
        addEntry(".wpd", APPLICATION_WORDPERFECT);
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
        addEntry(".xl", APPLICATION_EXCEL);
        addEntry(".xla", APPLICATION_EXCEL);
        addEntry(".xla", APPLICATION_X_EXCEL);
        addEntry(".xla", "application/x-msexcel");
        addEntry(".xlb", APPLICATION_EXCEL);
        addEntry(".xlb", APPLICATION_VND_MS_EXCEL);
        addEntry(".xlb", APPLICATION_X_EXCEL);
        addEntry(".xlc", APPLICATION_EXCEL);
        addEntry(".xlc", APPLICATION_VND_MS_EXCEL);
        addEntry(".xlc", APPLICATION_X_EXCEL);
        addEntry(".xld", APPLICATION_EXCEL);
        addEntry(".xld", APPLICATION_X_EXCEL);
        addEntry(".xlk", APPLICATION_EXCEL);
        addEntry(".xlk", APPLICATION_X_EXCEL);
        addEntry(".xll", APPLICATION_EXCEL);
        addEntry(".xll", APPLICATION_VND_MS_EXCEL);
        addEntry(".xll", APPLICATION_X_EXCEL);
        addEntry(".xlm", APPLICATION_EXCEL);
        addEntry(".xlm", APPLICATION_VND_MS_EXCEL);
        addEntry(".xlm", APPLICATION_X_EXCEL);
        addEntry(".xls", APPLICATION_VND_MS_EXCEL);
        addEntry(".xls", APPLICATION_X_EXCEL);
        addEntry(".xls", "application/x-msexcel");
        addEntry(".xls", APPLICATION_EXCEL);
        // addEntry(".xlsx","application/excel");
        addEntry(".xlt", APPLICATION_EXCEL);
        addEntry(".xlt", APPLICATION_X_EXCEL);
        addEntry(".xlv", APPLICATION_EXCEL);
        addEntry(".xlv", APPLICATION_X_EXCEL);
        addEntry(".xlw", APPLICATION_EXCEL);
        addEntry(".xlw", APPLICATION_VND_MS_EXCEL);
        addEntry(".xlw", APPLICATION_X_EXCEL);
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
        addEntry(".z", APPLICATION_X_COMPRESSED);
        addEntry(".zip", APPLICATION_X_COMPRESSED);
        addEntry(".zip", "application/x-zip-compressed");
        addEntry(".zip", "application/zip");
        addEntry(".zip", "multipart/x-zip");
        addEntry(".zoo", APPLICATION_OCTET_STREAM);
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

}
