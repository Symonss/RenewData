package com.dennohpeter.renewdata;

import android.content.Context;

import com.michaelflisar.changelog.tags.IChangelogTag;

public class CustomTags implements IChangelogTag {
    private String tag_name;
    private String prefix;

    CustomTags(String tag_name, String prefix) {
        this.tag_name = tag_name;
        this.prefix = prefix;
    }

    @Override
    public String getXMLTagName() {
        return tag_name;
    }

    @Override
    public String formatChangelogRow(Context context, String changeText) {
        return prefix + changeText;
    }
}
