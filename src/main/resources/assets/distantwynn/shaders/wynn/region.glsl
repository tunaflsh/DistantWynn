struct Region {
    ivec4 min; // BlockBox.min()
    ivec4 max; // BlockBox.max() (invlusive)
};

layout(binding = REGION_UNIFORM_BINDING, std140) uniform RegionUniform {
    Region wynnRegion;
    Region voxyRegion;
};

bool outsideRegion(in UnpackedNode node, in Region region) {
    if (region.min.w == 0) return false; // region == null
    ivec3 min = (node.pos << node.lodLevel) << 5;
    ivec3 max = ((node.pos + 1) << node.lodLevel) << 5;
    return any(lessThanEqual(max, region.min.xyz)) || any(greaterThan(min, region.max.xyz));
}
