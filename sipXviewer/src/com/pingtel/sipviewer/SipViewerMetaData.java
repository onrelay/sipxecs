/*
 * Copyright (C) 2010 Avaya, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package com.pingtel.sipviewer;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;

import com.pingtel.sipviewer.PopUpUtils.TimeDisplayMode;

public class SipViewerMetaData
{

    // local reference to the SIPViewerFrame
    // so that it can be used in various methods here without
    // passing it around all the time
    protected static SIPViewerFrame m_frame;

    // input is the root container of the input file, it contains individual
    // XML elements that are SIP messages
    public static void setSipViewerMetaData(SIPViewerFrame frame) {

        m_frame = frame;

        // first we see if sipviewer_meta data is embedded in the XML file
        Element sipviewerMeta = getChild(SipBranchData.nodeContainer, "sipviewer_meta");

        // if we have an entry in the list then we got the data
        if (sipviewerMeta != null) {

            // set the column locations
            setKeyLocations(getChild(sipviewerMeta, "locations"), m_frame.m_header);

            // set the background colors
            setBackgroundColors(getChild(sipviewerMeta, "colors"), m_frame.m_model);

            if (getChild(sipviewerMeta, "display_locations") != null) {
                setDisplayLocations(getChild(sipviewerMeta, "display_locations"), m_frame.m_model);
            }

            // sets the usage counts for each vertical line to know
            // how many messages "connect" to it
            if (getChild(sipviewerMeta, "usage_counts") != null) {
                setUsageCounts(getChild(sipviewerMeta, "usage_counts"), m_frame.m_model);
            }

            // set the view mode single/split
            setViewMode(getChildText(sipviewerMeta, "mode"), m_frame);

            // first see if time info is in the log file
            String timeIndexFormat = getChildText(sipviewerMeta, "time_index_format");

            if (timeIndexFormat != null) {
                // set the current display mode
                PopUpUtils.currentTimeDisplaySelection = TimeDisplayMode.valueOf(timeIndexFormat);

                // set the key index
                PopUpUtils.keyIndex = Integer.valueOf(getChildText(sipviewerMeta, "time_index_key"));

                // set the time zone value
                setTimeZone(getChildText(sipviewerMeta, "time_zone"), m_frame);

                // set the time index mode
                setTimeIndexMode(getChildText(sipviewerMeta, "time_index_mode"), m_frame);
            }

            // set the scroll locations for each pane
            setScrollLocations(getChild(sipviewerMeta, "scroll_locations"), m_frame.m_scrollPane,
                    m_frame.m_scrollPaneSecond);
        }

        // determine the time index values
        PopUpUtils.setTimeIndex(m_frame);

        // repaint the time index columns
        m_frame.m_bodyTimeIndex.revalidate();
        m_frame.m_bodyTimeIndex.repaint();
        m_frame.m_bodyTimeIndexSecond.revalidate();
        m_frame.m_bodyTimeIndexSecond.repaint();

        // refresh the frame
        m_frame.validate();
        m_frame.repaint();
    }

    // sets location of each column, locations are extracted from the input XML file
    private static void setKeyLocations(Element locations, ChartHeader m_header) {
        NodeList elementList = locations.getElementsByTagName("location");

        for (int i = 0; i < elementList.getLength(); i++) {
            Element location = (Element) elementList.item(i);
            m_header.setKeyPosition(i, Double.valueOf(location.getTextContent().trim()));
        }
    }

    // sets background colors for each label/arrow combination
    private static void setBackgroundColors(Element colors, SIPChartModel m_model) {
        NodeList elementList = colors.getElementsByTagName("color");

        for (int i = 0; i < elementList.getLength(); i++) {
            Element color = (Element) elementList.item(i);
            Color tmpColor = new Color(Integer.valueOf(color.getTextContent().trim()));

            if (tmpColor.getRGB() == Color.BLACK.getRGB()) {
                m_model.getEntryAt(i).backgroundColor = Color.BLACK;
            } else {
                m_model.getEntryAt(i).backgroundColor = tmpColor;
            }
        }
    }

    // sets the display index for each message
    private static void setDisplayLocations(Element displayLocations, SIPChartModel m_model) {
        NodeList elementList = displayLocations.getElementsByTagName("display_location");

        for (int i = 0; i < elementList.getLength(); i++) {
            Element displayLocation = (Element) elementList.item(i);
            m_model.getEntryAt(i).displayIndex = Integer.valueOf(displayLocation.getTextContent().trim());
        }
    }

    // sets the usage counts for all the columns
    private static void setUsageCounts(Element usageCounts, SIPChartModel m_model) {
        NodeList elementList = usageCounts.getElementsByTagName("usage_count");

        for (int x = 0; x < m_model.m_iNumKeys; x++) {
            Element usageCount = (Element) elementList.item(x);
            m_model.m_keyUsage[x] = Integer.valueOf(usageCount.getTextContent().trim());
        }
    }

    // sets the view mode (single or split screen)
    private static void setViewMode(String mode, SIPViewerFrame m_frame) {
        if ("split".equalsIgnoreCase(mode)) {
            m_frame.setSecondPaneVisibility(true);
        }
    }

    // sets the time zone
    private static void setTimeZone(String mode, SIPViewerFrame m_frame) {
        if ("utc".equalsIgnoreCase(mode)) {
            m_frame.m_utcTimeZone.setSelected(true);
        } else {
            m_frame.m_localTimeZone.setSelected(true);
        }
    }

    // sets the time index visibility
    private static void setTimeIndexMode(String mode, SIPViewerFrame m_frame) {
        if ("invisible".equalsIgnoreCase(mode)) {
            m_frame.m_scrollPaneTimeIndex.setVisible(false);
            m_frame.m_scrollPaneTimeIndexSecond.setVisible(false);
        } else if (m_frame.m_scrollPaneSecond.isVisible()) {
            m_frame.m_bodyTimeIndexSecond.setVisible(true);
        }
    }

    // sets the scroll bar locations for both panes
    private static void setScrollLocations(Element scrollLocations, JScrollPane m_scrollPane,
            JScrollPane m_scrollPaneSecond) {
        NodeList elementList = scrollLocations.getElementsByTagName("scroll_location");

        for (int i = 0; i < elementList.getLength(); i++) {
            Element scrollLocation = (Element) elementList.item(i);
            int maxBarSpan;

            if (i == SIPViewerFrame.topPaneID) {
                maxBarSpan = m_scrollPane.getVerticalScrollBar().getMaximum();
                m_scrollPane.getVerticalScrollBar().setValue(
                        (int) (maxBarSpan * Double.valueOf(scrollLocation.getTextContent().trim())));
            } else {
                maxBarSpan = m_scrollPaneSecond.getVerticalScrollBar().getMaximum();
                m_scrollPaneSecond.getVerticalScrollBar().setValue(
                        (int) (maxBarSpan * Double.valueOf(scrollLocation.getTextContent().trim())));
            }
        }
    }

public static void saveSipViewerMetaData(SIPViewerFrame frameRef, String openedFileName,
        ChartHeader m_header, SIPChartModel m_model, SIPViewerFrame m_frame,
        JScrollPane m_scrollPane, JScrollPane m_scrollPaneSecond) {

    JPanel accesoryPanel = new JPanel(new GridLayout(2, 1));
    accesoryPanel.setBorder(BorderFactory.createTitledBorder("Save options"));

    JCheckBox checkbox = new JCheckBox("Omit Invisible Dialogs", null);
    checkbox.setSelected(false);

    SaveOptionCheckBox checkBoxListener = new SaveOptionCheckBox(checkbox);

    checkbox.addActionListener(checkBoxListener);
    accesoryPanel.add(checkbox);

    // Create a file chooser
    JFileChooser fc = new JFileChooser();
    FileNameExtensionFilter filter = new FileNameExtensionFilter("XML Log", "xml");
    fc.setFileFilter(filter);
    fc.setAccessory(accesoryPanel);
    fc.setSelectedFile(new File(openedFileName));

    // Show the "save" file dialog
    int returnVal = fc.showSaveDialog(frameRef);

    // if user pressed save lets do the input checking
    if (returnVal == JFileChooser.APPROVE_OPTION) {
        File fileName = fc.getSelectedFile();

        // if user actually entered something process the input
        if ((fileName.getName() != null) && (fileName.getName().length() > 0)) {
            // file must end with a png extension, make user type it in
            if (!fileName.getName().toLowerCase().endsWith(".xml")) {
                JOptionPane.showMessageDialog(null, "Error: file name must end with \".xml\".",
                        "Wrong file name", 1);
            } else {
                // used to show the save file selector
                File saveFile = fc.getSelectedFile();

                try {
                    // Create a new DOM Document
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.newDocument();

                    // Remove existing "sipviewer_meta" if present
                    NodeList metaNodes = SipBranchData.nodeContainer.getElementsByTagName("sipviewer_meta");
                    if (metaNodes.getLength() > 0) {
                        Node metaNode = metaNodes.item(0);
                        SipBranchData.nodeContainer.removeChild(metaNode);
                    }

                    // Add new "sipviewer_meta" content
                    Element metaData = constructMetaData(m_header, m_model, m_frame, m_scrollPane, m_scrollPaneSecond, doc);
                    SipBranchData.nodeContainer.appendChild(metaData);

                    // Write the updated XML to the file
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                    DOMSource source = new DOMSource(SipBranchData.nodeContainer);
                    StreamResult result = new StreamResult(new FileWriter(saveFile.getAbsoluteFile()));
                    transformer.transform(source, result);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    } else {
        // user canceled the request do nothing
    }
}

// constructs the sipviewer_meta data component of the XML log file
public static Element constructMetaData(ChartHeader m_header, SIPChartModel m_model,
        SIPViewerFrame m_frame, JScrollPane m_scrollPane, JScrollPane m_scrollPaneSecond, Document doc) {
    // creating top-level elements for constructing the sipviewer_meta XML section
    Element meta = doc.createElement("sipviewer_meta");
    Element locations = doc.createElement("locations");
    Element usage_counts = doc.createElement("usage_counts");
    Element colors = doc.createElement("colors");
    Element display_locations = doc.createElement("display_locations");
    Element scroll_locations = doc.createElement("scroll_locations");

    // getting current column locations
    double keyPositions[] = m_header.getKeyPositions();

    // storing the column locations in XML format
    for (int x = 0; x < m_model.m_iNumKeys; x++) {
        // if user wants to delete invisible messages then
        // any columns that have no usage against them,
        // basically that have no messages "connecting"
        // to them should not be saved
        if (SaveOptionCheckBox.getCheckStatus()) {
            // only save column location if it has visible messages against it
            if ((m_model.m_keyUsage[x] != 0)) {
                Element location = doc.createElement("location");
                location.setTextContent(Double.toString(keyPositions[x]));

                // storing the locations under the "locations" XML tag
                locations.appendChild(location);

                // now lets store the usage count for the columns, basically
                // how many times are they really a target or source of a message
                Element usage_count = doc.createElement("usage_count");
                usage_count.setTextContent(Integer.toString(m_model.m_keyUsage[x]));

                // storing the usage_count under the "locations" XML tag
                usage_counts.appendChild(usage_count);
            }
        } else {
            // user does not want to remove the invisible
            // messages so make sure to save all column locations
            // regardless of their key use
            Element location = doc.createElement("location");
            location.setTextContent(Double.toString(keyPositions[x]));

            // storing the locations under the "locations" XML tag
            locations.appendChild(location);

            // now lets store the usage count for the columns
            Element usage_count = doc.createElement("usage_count");
            usage_count.setTextContent(Integer.toString(m_model.m_keyUsage[x]));

            // storing the usage_count under the "locations" XML tag
            usage_counts.appendChild(usage_count);
        }
    }

    // adding locations and usage counts to the meta data component
    meta.appendChild(locations);
    meta.appendChild(usage_counts);

    // if the delete invisible message check box is checked
    // then we have to remove the messages from the XML structure
    if (SaveOptionCheckBox.getCheckStatus()) {
        // get the list of branchNode elements
        NodeList elementList = SipBranchData.nodeContainer.getElementsByTagName("branchNode");

        // loop through all the elements backwards since once
        // we start deleting the indexes shift, going backwards
        // the lower indexes are still accurate
        for (int i = elementList.getLength() - 1; i >= 0; i--) {
            // if the message is invisible then remove it
            if (m_model.getEntryAt(i).displayIndex < 0) {
                Node node = elementList.item(i);
                SipBranchData.nodeContainer.removeChild(node);
            }
        }
    }

    // storing the message locations and color in XML format
    for (int x = 0; x < m_model.getSize(); x++) {
        Element color = doc.createElement("color");
        Element display_location = doc.createElement("display_location");

        // if the box is checked to delete all hidden messages
        // then we don't want to store display_locations of
        // those messages, basically if displayIndex is < 0 we
        // skip it
        if (SaveOptionCheckBox.getCheckStatus()) {
            // if displayIndex is of a visible message lets
            // store its index in the XML structure, else
            // we skip the message
            if (m_model.getEntryAt(x).displayIndex >= 0) {
                color.setTextContent(Integer.toString(m_model.getEntryAt(x).backgroundColor.getRGB()));

                // storing the colors under the "colors" XML tag
                colors.appendChild(color);

                display_location.setTextContent(Integer.toString(m_model.getEntryAt(x).displayIndex));

                // storing the message location under "display_location" XML tag
                display_locations.appendChild(display_location);
            }
        } else {
            color.setTextContent(Integer.toString(m_model.getEntryAt(x).backgroundColor.getRGB()));

            // storing the colors under the "colors" XML tag
            colors.appendChild(color);

            // user wants to keep all the messages as they are
            display_location.setTextContent(Integer.toString(m_model.getEntryAt(x).displayIndex));

            // storing the message location under "display_location" XML tag
            display_locations.appendChild(display_location);
        }
    }

    // adding colors and display_locations to the meta data component
    meta.appendChild(colors);
    meta.appendChild(display_locations);

    // mode is a single entry so don't need a top XML container for it
    Element mode = doc.createElement("mode");

    if (m_frame.getPaneVisibility(SIPViewerFrame.bottomPaneID)) {
        // if the bottom panel is visible then we are in the split mode
        mode.setTextContent("split");
    } else {
        // if the bottom panel is not visible then we are in the single mode
        mode.setTextContent("single");
    }

    meta.appendChild(mode);

    // time_index_mode is a single entry so don't need a top XML container for it
    Element time_index_mode = doc.createElement("time_index_mode");

    if (m_frame.m_scrollPaneTimeIndex.isVisible()) {
        // if the top time index column is visible so
        // that means that time index columns are set
        // to visible
        time_index_mode.setTextContent("visible");
    } else {
        // top time index column is not visible so
        // user has selected to hide time index columns
        time_index_mode.setTextContent("invisible");
    }

    // adding time_index_mode to the meta data component
    meta.appendChild(time_index_mode);

    // mode is a single entry so don't need a top XML container for it
    Element time_index_format = doc.createElement("time_index_format");

    // lets get the time display format that is currently selected
    time_index_format.setTextContent(String.valueOf(PopUpUtils.currentTimeDisplaySelection));

    // adding time_index_format to the meta data component
    meta.appendChild(time_index_format);

    // mode is a single entry so don't need a top XML container for it
    Element time_index_key = doc.createElement("time_index_key");

    // if user decided to omit invisible dialogs
    if (SaveOptionCheckBox.getCheckStatus()) {
        // if the key index is invisible, the dialog that has the key
        // index is invisible, then we can't store the key index
        // value in the xml file since it will be invalid on
        // next file load, because the hidden messages would
        // have been removed
        if (m_model.getEntryAt(PopUpUtils.keyIndex).displayIndex < 0) {
            // if message is invisible set key index to default
            // value of 0
            time_index_key.setTextContent("0");
        } else {
            // else store key index
            time_index_key.setTextContent(String.valueOf(PopUpUtils.keyIndex));
        }
    } else {
        // also if user decides not to omit any dialogs then we
        // can safely store the key index since the message that
        // its set against will be stored in the log file
        time_index_key.setTextContent(String.valueOf(PopUpUtils.keyIndex));
    }

    // adding mode to the meta data component
    meta.appendChild(time_index_key);

    // time_zone is a single entry so don't need a top XML container for it
    Element time_zone = doc.createElement("time_zone");

    if (m_frame.m_utcTimeZone.isSelected()) {
        // lets get the time zone selected
        time_zone.setTextContent("utc");
    } else {
        // user is using local time zone
        time_zone.setTextContent("local");
    }

    // adding time_zone to the meta data component
    meta.appendChild(time_zone);

    // creating and storing the relative scroll position of the top and
    // bottom panels
    Element scroll_location1 = doc.createElement("scroll_location");

    scroll_location1.setTextContent(Double.toString((double) m_scrollPane.getVerticalScrollBar()
            .getValue()
            / (double) m_scrollPane.getVerticalScrollBar().getMaximum()));
    scroll_locations.appendChild(scroll_location1);

    Element scroll_location2 = doc.createElement("scroll_location");

    scroll_location2.setTextContent(Double.toString((double) m_scrollPaneSecond.getVerticalScrollBar()
            .getValue()
            / (double) m_scrollPaneSecond.getVerticalScrollBar().getMaximum()));
    scroll_locations.appendChild(scroll_location2);

    // adding the scroll positions to the meta data component
    meta.appendChild(scroll_locations);

    return meta;
}

    // this action listener is envoked when user presses the check box to
    // omit invisible messages
    static public class SaveOptionCheckBox extends JDialog implements ActionListener
    {
        static JCheckBox checkboxRef;

        // class constructor
        public SaveOptionCheckBox(JCheckBox checkbox) {
            checkboxRef = checkbox;
            checkboxRef.setSelected(false);
        }

        // handles the events, if user clicks on one of the buttons the
        // selectedColor is assigned its value and the color chooser
        // dialog disappears
        public void actionPerformed(ActionEvent e)
        {

            if ((checkboxRef.isSelected())
                    && (m_frame.m_model.getEntryAt(PopUpUtils.keyIndex).displayIndex < 0))
            {
                if (PopUpUtils.currentTimeDisplaySelection == TimeDisplayMode.SINCE_KEY_INDEX)
                {
                    JOptionPane
                            .showMessageDialog(
                                    null,
                                    "The Time Disply Format is set to \"Since Key Index\". You assigned the Key\n"
                                            + "Index to a dialog that is no longer visible. If you choose to Omit Invisible\n"
                                            + "Dialogs then your current time index values will not be reproducable from\n"
                                            + "the saved log file and the Key Index position will be reset to 0.",
                                    "SipViewer Annotation Info Loss", JOptionPane.WARNING_MESSAGE);
                }
                else
                {
                    JOptionPane
                            .showMessageDialog(
                                    null,
                                    "You assigned the Key Index to a dialog that is no longer visible. If you choose\n"
                                            + "to Omit Invisible Dialogs then the Key Index position will be reset to 0.",
                                    "SipViewer Annotation Info Loss", JOptionPane.WARNING_MESSAGE);
                }
            }
        }

        static public boolean getCheckStatus()
        {
            return checkboxRef.isSelected();
        }

    }

    private static String getChildText(Element parent, String tagName) {
        Element child = getChild(parent, tagName);
        return child != null ? child.getTextContent().trim() : "";
    }

    private static Element getChild(Element parent, String tagName) {
        NodeList list = parent.getElementsByTagName(tagName);
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getParentNode().equals(parent) && node.getNodeType() == Node.ELEMENT_NODE) {
                return (Element) node;
            }
        }
        return null;
    }
}
