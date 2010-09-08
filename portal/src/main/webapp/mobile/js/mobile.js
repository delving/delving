function resizeIEMobile(img, maxw, maxh) {
    if (img && maxh && maxw) {
        if (img.width >= maxw) {
            img.height = Math.round(img.height*(maxw/img.width));
            img.width = maxw;
        }
        if (img.height >= maxh) {
            img.width = Math.round(img.width*(maxh/img.height));
            img.height = maxh;
        }
    }
};