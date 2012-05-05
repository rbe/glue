/*
 * Glue, https://github.com/rbe/glue
 * Copyright (C) 2009-2010 Informationssysteme Ralf Bensmann.
 * Copyright (C) 2011-2012 art of coding UG (haftungsbeschränkt).
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.bensmann.glue

/**
 * 
 */
class GlueMimeType {
	
	/**
	 * Name of this mime type.
	 */
	String name
	
	/**
	 * Filename extension.
	 */
	String extension
	
	/**
	 * Representation of this mime type when sending from a web server.
	 */
	String browser
	
	/**
	 * Description.
	 */
	String description
	
	static mapping = {
		table "T0_MIME"
	}
	
	static constraints = {
		name(nullable: false)
		extension(nullable: true, unique: true)
		browser(nullable: true)
		description(nullable: true)
	}
	
	/**
	 * See http://de.selfhtml.org/diverses/mimetypen.htm.
	 */
	static mimeTypes = [
		[name: "Unknown", extension: "*", browser: "application/octet-stream"],
		[name: "OpenOffice.org Writer (odt)", extension: "odt", browser: "application/vnd.oasis.opendocument.text"],
		[name: "OpenOffice.org Writer Template (ott)", extension: "ott", browser: "application/vnd.oasis.opendocument.text-template"],
		[name: "OpenOffice.org Writer Web (oth)", extension: "oth", browser: "application/vnd.oasis.opendocument.text-web"],
		[name: "OpenOffice.org Writer Masterdocument (odm)", extension: "odm", browser: "application/vnd.oasis.opendocument.text-master"],
		[name: "OpenOffice.org Draw (odg)", extension: "odg", browser: "application/vnd.oasis.opendocument.graphics"],
		[name: "OpenOffice.org Draw Template (otg)", extension: "otg", browser: "application/vnd.oasis.opendocument.graphics-template"],
		[name: "OpenOffice.org Impress (odp)", extension: "odp", browser: "application/vnd.oasis.opendocument.presentation"],
		[name: "OpenOffice.org Impress Template (otp)", extension: "otp", browser: "application/vnd.oasis.opendocument.presentation-template"],
		[name: "OpenOffice.org Calc (ods)", extension: "ods", browser: "application/vnd.oasis.opendocument.spreadsheet"],
		[name: "OpenOffice.org Calc Template (ots)", extension: "ots", browser: "application/vnd.oasis.opendocument.spreadsheet-template"],
		[name: "OpenOffice.org Chart (odc)", extension: "odc", browser: "application/vnd.oasis.opendocument.chart"],
		[name: "OpenOffice.org Formula (odf)", extension: "odf", browser: "application/vnd.oasis.opendocument.formula"],
		[name: "OpenOffice.org Image (odi)", extension: "odi", browser: "application/vnd.oasis.opendocument.image"],
		[name: "JPEG (jpg)", extension: "jpg", browser: "image/jpeg"],
		[name: "JPEG (jpeg)", extension: "jpeg", browser: "image/jpeg"],
		[name: "TIFF (tif)", extension: "tif", browser: "image/tif"],
		[name: "TIFF (tiff)", extension: "tiff", browser: "image/tif"],
		[name: "Portable Network Graphics (png)", extension: "png", browser: "image/png"],
		[name: "Portable Document Format (pdf)", extension: "pdf", browser: "application/pdf"],
		[name: "Rich Text Format (rtf)", extension: "rtf", browser: "application/rtf"],
		[name: "Microsoft Office Word 97-2003 (doc)", extension: "doc", browser: "application/msword"],
		[name: "Microsoft Office Word 97-2003 Template (dot)", extension: "dot", browser: "application/msword"],
		[name: "Microsoft Office Excel 97-2003 (xls)", extension: "xls", browser: "application/vnd.ms-excel"],
		[name: "Microsoft Office Excel 97-2003 Template (xlt)", extension: "xlt", browser: "application/vnd.ms-excel"],
		[name: "Microsoft Office Excel 97-2003 Addin (xla)", extension: "xla", browser: "application/vnd.ms-excel"],
		[name: "Microsoft Office PowerPoint 97-2003 (ppt)", extension: "ppt", browser: "application/vnd.ms-powerpoint"],
		[name: "Microsoft Office PowerPoint 97-2003 Template (pot)", extension: "pot", browser: "application/vnd.ms-powerpoint"],
		[name: "Microsoft Office PowerPoint 97-2003 (pps)", extension: "pps", browser: "application/vnd.ms-powerpoint"],
		[name: "Microsoft Office PowerPoint 97-2003 (ppa)", extension: "ppa", browser: "application/vnd.ms-powerpoint"],
		[name: "Microsoft Office Word 2007 (docx)", extension: "docx", browser: "application/vnd.openxmlformats-officedocument.wordprocessingml.document"],
		[name: "Microsoft Office Word 2007 Template (dotx)", extension: "dotx", browser: "application/vnd.openxmlformats-officedocument.wordprocessingml.template"],
		[name: "Microsoft Office Word 2007 (docm)", extension: "docm", browser: "application/vnd.ms-word.document.macroEnabled.12"],
		[name: "Microsoft Office Word 2007 Template (dotm)", extension: "dotm", browser: "application/vnd.ms-word.template.macroEnabled.12"],
		[name: "Microsoft Office Excel 2007 (xlsx)", extension: "xlsx", browser: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"],
		[name: "Microsoft Office Excel 2007 Template (xltx)", extension: "xltx", browser: "application/vnd.openxmlformats-officedocument.spreadsheetml.template"],
		[name: "Microsoft Office Excel 2007 (xlsm)", extension: "xlsm", browser: "application/vnd.ms-excel.sheet.macroEnabled.12"],
		[name: "Microsoft Office Excel 2007 Template (xltm)", extension: "xltm", browser: "application/vnd.ms-excel.template.macroEnabled.12"],
		[name: "Microsoft Office Excel 2007 Addin (xlam)", extension: "xlam", browser: "application/vnd.ms-excel.addin.macroEnabled.12"],
		[name: "Microsoft Office Excel 2007 Binary (xlsb)", extension: "xlsb", browser: "application/vnd.ms-excel.sheet.binary.macroEnabled.12"],
		[name: "Microsoft Office PowerPoint 2007 (pptx)", extension: "pptx", browser: "application/vnd.openxmlformats-officedocument.presentationml.presentation"],
		[name: "Microsoft Office PowerPoint 2007 Template (potx)", extension: "potx", browser: "application/vnd.openxmlformats-officedocument.presentationml.template"],
		[name: "Microsoft Office PowerPoint 2007 (ppsx)", extension: "ppsx", browser: "application/vnd.openxmlformats-officedocument.presentationml.slideshow"],
		[name: "Microsoft Office PowerPoint 2007 Addin (ppam)", extension: "ppam", browser: "application/vnd.ms-powerpoint.addin.macroEnabled.12"],
		[name: "Microsoft Office PowerPoint 2007 (pptm)", extension: "pptm", browser: "application/vnd.ms-powerpoint.presentation.macroEnabled.12"],
		[name: "Microsoft Office PowerPoint 2007 Template (potm)", extension: "potm", browser: "application/vnd.ms-powerpoint.template.macroEnabled.12"],
		[name: "Microsoft Office PowerPoint 2007 Slideshow (ppsm)", extension: "ppsm", browser: "application/vnd.ms-powerpoint.slideshow.macroEnabled.12"]
	]
	
}
