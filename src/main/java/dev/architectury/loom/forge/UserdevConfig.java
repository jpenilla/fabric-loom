package dev.architectury.loom.forge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.loom.configuration.providers.forge.ForgeRunTemplate;

public record UserdevConfig(
		String mcp,
		String universal,
		String sources,
		String patches,
		Optional<String> patchesOriginalPrefix,
		Optional<String> patchesModifiedPrefix,
		String binpatches,
		BinaryPatcherConfig binpatcher,
		List<String> libraries,
		Map<String, ForgeRunTemplate> runs,
		List<String> sass
) {
	public static final Codec<UserdevConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("mcp").forGetter(UserdevConfig::mcp),
			Codec.STRING.fieldOf("universal").forGetter(UserdevConfig::universal),
			Codec.STRING.fieldOf("sources").forGetter(UserdevConfig::sources),
			Codec.STRING.fieldOf("patches").forGetter(UserdevConfig::patches),
			Codec.STRING.optionalFieldOf("patchesOriginalPrefix").forGetter(UserdevConfig::patchesOriginalPrefix),
			Codec.STRING.optionalFieldOf("patchesModifiedPrefix").forGetter(UserdevConfig::patchesModifiedPrefix),
			Codec.STRING.fieldOf("binpatches").forGetter(UserdevConfig::binpatches),
			BinaryPatcherConfig.CODEC.fieldOf("binpatcher").forGetter(UserdevConfig::binpatcher),
			Codec.STRING.listOf().xmap(UserdevConfig::fixDependencies, Function.identity()).fieldOf("libraries").forGetter(UserdevConfig::libraries),
			ForgeRunTemplate.MAP_CODEC.fieldOf("runs").forGetter(UserdevConfig::runs),
			Codec.STRING.listOf().optionalFieldOf("sass", List.of()).forGetter(UserdevConfig::sass)
	).apply(instance, UserdevConfig::new));

	private static List<String> fixDependencies(List<String> deps) {
		final List<String> newDeps = new ArrayList<>(deps);

		if (deps.stream().anyMatch(dep -> dep.startsWith("com.google.guava:guava:"))) {
			// Work around issues caused by the dependency tree being flattened
			newDeps.remove("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava@jar");
		}

		return Collections.unmodifiableList(newDeps);
	}

	public record BinaryPatcherConfig(String dependency, List<String> args) {
		public static final Codec<BinaryPatcherConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("version").forGetter(BinaryPatcherConfig::dependency),
				Codec.STRING.listOf().fieldOf("args").forGetter(BinaryPatcherConfig::args)
		).apply(instance, BinaryPatcherConfig::new));
	}
}
