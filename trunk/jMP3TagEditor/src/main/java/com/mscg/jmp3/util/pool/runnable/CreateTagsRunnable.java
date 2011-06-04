package com.mscg.jmp3.util.pool.runnable;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultListModel;

import com.mscg.jID3tags.file.MP3File;
import com.mscg.jID3tags.id3v2.ID3v2Tag;
import com.mscg.jID3tags.objects.frames.ID3v2APICFrame;
import com.mscg.jID3tags.objects.frames.contents.ID3v2FrameContentImage;
import com.mscg.jID3tags.util.Costants.PictureType;
import com.mscg.jID3tags.util.Costants.StringEncodingType;
import com.mscg.jmp3.exception.InvalidRegExTagValueException;
import com.mscg.jmp3.exception.InvalidTagValueException;
import com.mscg.jmp3.i18n.Messages;
import com.mscg.jmp3.main.AppLaunch;
import com.mscg.jmp3.transformator.StringTransformator;
import com.mscg.jmp3.ui.panel.fileoperations.TagFromFilenameTab;
import com.mscg.jmp3.ui.panel.fileoperations.dialog.ExecuteTagCreationDialog;
import com.mscg.jmp3.ui.renderer.elements.IconAndFileListElement;
import com.mscg.jmp3.util.Util;

public class CreateTagsRunnable extends GenericFileOperationRunnable {

    private List<File> files;
    private TagFromFilenameTab tab;
    private Pattern regExGroupPattern;
    private static final FileNameMap fileNameMap = URLConnection.getFileNameMap();

    public CreateTagsRunnable(ExecuteTagCreationDialog dialog) {
        super(dialog);
        this.files = new LinkedList<File>();
        DefaultListModel listModel = (DefaultListModel)AppLaunch.mainWindow.getFileChooseCard().getFilesList().getModel();
        for(Object listElement : listModel.toArray()) {
            IconAndFileListElement fileListEl = (IconAndFileListElement) listElement;
            files.add(fileListEl.getFile());
        }
        this.tab = dialog.getTab();
        regExGroupPattern = Pattern.compile("^\\s*%(\\d+)\\s*$");
    }

    @Override
    public void executeInterruptible() {
        Pattern fileNamePattern = null;
        String value = null;
        BoundedRangeModel rangeModel = dialog.getProgressBar().getModel();
        rangeModel.setMinimum(0);
        rangeModel.setMaximum(2 * files.size());
        try {
            value = tab.getRegExpPanel().getValue();
            if(value != null && value.length() != 0) {
                fileNamePattern = Pattern.compile(value);
            }
            int progess = 0;
            for(File file : files) {
                if(isInterrupted())
                    return;

                dialog.getActualFileName().setText(file.getName());
                rangeModel.setValue(++progess);

                value = null;
                Matcher fileNameMatcher = null;
                String fieldKey = null;
                try {
                    String startName = file.getName();
                    int index = startName.toLowerCase().lastIndexOf(".mp3");
                    startName = startName.substring(0, index);

                    for(StringTransformator transformator : tab.getTransformationsPanel().getTransformators()) {
                        startName = transformator.transformString(startName);
                    }

                    if(fileNamePattern != null) {
                        fileNameMatcher = fileNamePattern.matcher(startName);
                        fileNameMatcher.find();
                    }

                    MP3File mp3File = new MP3File(file);

                    if(!mp3File.hasID3v2tag())
                        mp3File.setID3v2tag(new ID3v2Tag());

                    if(Boolean.parseBoolean(tab.getRemoveTagsPanel().getValue())) {
                        if(mp3File.getID3v2tag().getAllframes() != null)
                            mp3File.getID3v2tag().getAllframes().clear();
                        mp3File.setID3v11tag(null);
                    }

                    fieldKey = "operations.file.taginfo.info.author";
                    value = tab.getAuthorPanel().getValue();
                    if(Util.isNotEmptyOrWhiteSpaceOnly(value)) {
                        value = parseValue(value, fileNameMatcher);
                        mp3File.setArtist(value);
                    }

                    fieldKey = "operations.file.taginfo.info.album";
                    value = tab.getAlbumPanel().getValue();
                    if(Util.isNotEmptyOrWhiteSpaceOnly(value)) {
                        value = parseValue(value, fileNameMatcher);
                        mp3File.setAlbum(value);
                    }

                    fieldKey = "operations.file.taginfo.info.title";
                    value = tab.getTitlePanel().getValue();
                    if(Util.isNotEmptyOrWhiteSpaceOnly(value)) {
                        value = parseValue(value, fileNameMatcher);
                        mp3File.setTitle(value);
                    }

                    fieldKey = "operations.file.taginfo.info.number";
                    value = tab.getNumberPanel().getValue();
                    if(Util.isNotEmptyOrWhiteSpaceOnly(value)) {
                        value = parseValue(value, fileNameMatcher);
                        try {
                            Integer.parseInt(value, 10);
                        } catch(NumberFormatException e){
                            throw new InvalidTagValueException(e);
                        }
                        mp3File.setTrack(value);
                    }

                    fieldKey = "operations.file.taginfo.info.genre";
                    value = tab.getGenrePanel().getValue();
                    if(Util.isNotEmptyOrWhiteSpaceOnly(value)) {
                        value = parseValue(value, fileNameMatcher);
                        mp3File.setGenre(value);
                    }

                    fieldKey = "operations.file.taginfo.info.year";
                    value = tab.getYearPanel().getValue();
                    if(Util.isNotEmptyOrWhiteSpaceOnly(value)) {
                        value = parseValue(tab.getYearPanel().getValue(), fileNameMatcher);
                        try {
                            Integer.parseInt(value);
                        } catch(NumberFormatException e){
                            throw new InvalidTagValueException(e);
                        }
                        mp3File.setYear(value);
                    }

                    fieldKey = "operations.file.taginfo.info.cover";
                    value = tab.getCoverPanel().getValue();
                    if(Util.isNotEmptyOrWhiteSpaceOnly(value)) {
                        File coverFile = new File(value);
                        if(!coverFile.exists() || !coverFile.isFile())
                            throw new InvalidTagValueException("Invalid file specified");
                        ID3v2APICFrame picFrame = (ID3v2APICFrame)mp3File.getID3v2tag().getFrame(ID3v2APICFrame.id);
                        if(picFrame == null) {
                            picFrame = new ID3v2APICFrame();
                            mp3File.getID3v2tag().addFrame(picFrame);
                        }
                        ID3v2FrameContentImage imageContent = (ID3v2FrameContentImage)picFrame.getContent();
                        imageContent.setEncoding(StringEncodingType.ISO_8859_1);
                        imageContent.setPictureType(PictureType.Cover_front);
                        imageContent.setMimeType(fileNameMap.getContentTypeFor(coverFile.getName()));
                        imageContent.setPictureData(coverFile);
                    }

                    mp3File.copyID3v2ToID3v11();

                    mp3File.write();

                } catch(IndexOutOfBoundsException e) {
                    LOG.error("Cannot generate tag for file \"" + file.getAbsolutePath() + "\"", e);
                    AppLaunch.showError(new Exception(
                        Messages.getString("operations.file.error.regex.groupindex").
                            replace("${field}", Messages.getString(fieldKey))));
                    return;
                } catch(InvalidTagValueException e) {
                    LOG.error("Cannot generate tag for file \"" + file.getAbsolutePath() + "\"", e);
                    AppLaunch.showError(new Exception(
                        Messages.getString("operations.file.error.invalidfield").
                            replace("${field}", Messages.getString(fieldKey))));
                    return;
                } catch(InvalidRegExTagValueException e) {
                    LOG.error("Cannot generate tag for file \"" + file.getAbsolutePath() + "\"", e);
                    AppLaunch.showError(new Exception(
                        Messages.getString("operations.file.error.regex.undefined").
                            replace("${field}", Messages.getString(fieldKey))));
                    return;
                } catch(Exception e) {
                    LOG.error("Cannot generate tag for file \"" + file.getAbsolutePath() + "\"", e);
                    AppLaunch.showError(e);
                    return;
                }

                rangeModel.setValue(++progess);
            }

            AppLaunch.showMessage(Messages.getString("operations.file.taginfo.execute.done.title"),
                                  Messages.getString("operations.file.taginfo.execute.done.message").
                                      replace("${number}", "" + files.size()));
        } catch(PatternSyntaxException e) {
            LOG.error("Invalid regular expression", e);
            AppLaunch.showError(new Exception(Messages.getString("operations.file.error.regex").
                                                  replace("${regEx}", value)));
        } catch(Exception e) {
            LOG.error("Cannot generate tags", e);
            AppLaunch.showError(e);
        } finally {
            dialog.setVisible(false);
        }

    }

    private String parseValue(String value, Matcher fileNameMatcher) throws InvalidRegExTagValueException,
                                                                            InvalidTagValueException,
                                                                            IndexOutOfBoundsException {
        String ret = value;

        Matcher regExGroupMatcher = regExGroupPattern.matcher(value);
        if(regExGroupMatcher.find()) {
            if(fileNameMatcher  == null)
                throw new InvalidRegExTagValueException("Regular expression is not defined");
            int groupIndex = Integer.parseInt(regExGroupMatcher.group(1));
            try {
                ret = fileNameMatcher.group(groupIndex);
            } catch(IllegalStateException e) {
                throw new IndexOutOfBoundsException();
            }
        }

        return ret;
    }

}
