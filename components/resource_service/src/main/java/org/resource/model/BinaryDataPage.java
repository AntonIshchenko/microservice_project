package org.resource.model;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;

public class BinaryDataPage extends PageImpl<Byte> {

    private static int dataSize;

    public BinaryDataPage(List<Byte> content, Pageable pageable) {
        super(content, pageable, 0);
        dataSize = content.size();
    }

    @Override
    public List<Byte> getContent() {
        int fromIndex = getPageable().getPageNumber() * getPageable().getPageSize();
        if (fromIndex >= dataSize) {
            return Collections.emptyList();
        }
        int toIndex = Math.min((getPageable().getPageNumber() + 1) * getPageable().getPageSize(), dataSize);
        return super.getContent().subList(fromIndex, toIndex);
    }

}
