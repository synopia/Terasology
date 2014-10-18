/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.md5;

import com.google.common.collect.Lists;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetLoader;
import org.terasology.blender.api.BlenderParser;
import org.terasology.blender.mesh.Mesh;
import org.terasology.blender.mesh.Skeleton;
import org.terasology.module.Module;
import org.terasology.rendering.assets.skeletalmesh.Bone;
import org.terasology.rendering.assets.skeletalmesh.BoneWeight;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMeshData;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMeshDataBuilder;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * Created by synopia on 17.10.2014.
 */
public class BlenderMeshLoader implements AssetLoader<SkeletalMeshData> {
    private static final Logger logger = LoggerFactory.getLogger(BlenderMeshLoader.class);

    @Override
    public SkeletalMeshData load(Module module, InputStream stream, List<URL> urls, List<URL> deltas) throws IOException {
        BlenderParser parser = BlenderParser.load(stream);
        Mesh mesh = parser.getMesh();
        if (mesh == null) {
            logger.error("Cannot find mesh and armature in blend file!");
            return null;
        }
        Skeleton skeleton = mesh.getSkeleton();
        SkeletalMeshDataBuilder skeletonBuilder = new SkeletalMeshDataBuilder();

        List<Skeleton.Bone> bones = skeleton.getBones();
        List<Bone> sBones = Lists.newArrayList();
        for (int i = 0; i < bones.size(); i++) {
            Skeleton.Bone bone = bones.get(i);
            Vector3f position = new Vector3f(bone.getLocX(), bone.getLocY(), bone.getLocZ());
            Quat4f rotation = new Quat4f(bone.getQuatX(), bone.getQuatY(), bone.getQuatZ(), bone.getQuatW());
            MD5ParserCommon.CORRECTION_MATRIX.transform(position);
            rotation = MD5ParserCommon.correctQuat4f(rotation);
            Bone sBone = new Bone(i, bone.getName(), position, rotation);
            if (bone.getParent() != null) {
                Bone parent = sBones.get(bone.getParent().getId());
                parent.addChild(sBone);
            }
            sBones.add(sBone);
            skeletonBuilder.addBone(sBone);
        }
        List<Mesh.Weight> weights = mesh.getWeights();
        for (Mesh.Weight weight : weights) {
            skeletonBuilder.addWeight(new BoneWeight(new Vector3f(weight.getX(), weight.getY(), weight.getZ()), weight.getBias(), weight.getBone().getId()));
        }
        List<Mesh.Vertex> vertices = mesh.getVertices();
        List<Vector2f> uvs = Lists.newArrayList();
        TIntList vertexStartWeight = new TIntArrayList(vertices.size());
        TIntList vertexWeightCount = new TIntArrayList(vertices.size());
        for (Mesh.Vertex vertex : vertices) {
            uvs.add(new Vector2f(vertex.getU(), vertex.getV()));
            vertexStartWeight.add(vertex.getStart());
            vertexWeightCount.add(vertex.getSize());
        }
        skeletonBuilder.setUvs(uvs);
        skeletonBuilder.setVertexWeights(vertexStartWeight, vertexWeightCount);

        List<Mesh.Tri> tris = mesh.getTris();
        TIntList indices = new TIntArrayList(tris.size() * 3);
        for (Mesh.Tri tri : tris) {
            indices.add(tri.getV1());
            indices.add(tri.getV2());
            indices.add(tri.getV3());
        }
        skeletonBuilder.setIndices(indices);
        return skeletonBuilder.build();
    }

    public static void main(String[] args) throws IOException {
        BlenderMeshLoader loader = new BlenderMeshLoader();
        SkeletalMeshData load = loader.load(null, new FileInputStream("modules/DangerMod/assets/skeletalmesh/killerbunny.blend"), null, null);
        System.out.println(load.toMD5("texture.png"));
    }
}
