#!/usr/bin/env python3
#
# Author: John Starich
# License: APL2
#


from panflute import *


removed_top_header = False


def markdown_header(elem, doc):
    """
    Removes the first top level header (h1),
    sets the document title to that header's contents,
    and lowers the level of the other headers.
    """
    global removed_top_header
    if type(elem) != Header:
        return None
    if elem.level > 1:
        elem.level -= 1
        return None
    if removed_top_header is True:
        return None
    removed_top_header = True
    doc.metadata['title'] = MetaInlines(*elem.content)
    return []


if __name__ == '__main__':
    run_filter(markdown_header)
