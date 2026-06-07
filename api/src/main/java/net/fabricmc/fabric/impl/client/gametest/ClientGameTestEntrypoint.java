/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.impl.client.gametest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jspecify.annotations.Nullable;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;

public record ClientGameTestEntrypoint(IModInfo owner, String className,
	FabricClientGameTest entrypoint)
{
	public static List<ClientGameTestEntrypoint> getEntrypointContainers(
		String key)
	{
		List<ClientGameTestEntrypoint> entrypoints = new ArrayList<>();
		
		for(IModInfo mod : ModList.get().getMods())
		{
			Object value = mod.getModProperties().get(key);
			
			if(value == null)
			{
				continue;
			}
			
			if(!(value instanceof List<?> classNames))
			{
				throw new IllegalArgumentException(
					"Mod %s property %s must be a list of class names"
						.formatted(mod.getModId(), key));
			}
			
			for(Object className : classNames)
			{
				if(!(className instanceof String name) || name.isBlank())
				{
					throw new IllegalArgumentException(
						"Mod %s property %s contains a non-string or blank class name"
							.formatted(mod.getModId(), key));
				}
				
				entrypoints.add(new ClientGameTestEntrypoint(mod, name,
					newInstance(mod, name)));
			}
		}
		
		return Collections.unmodifiableList(entrypoints);
	}
	
	public FabricClientGameTest getEntrypoint()
	{
		return entrypoint;
	}
	
	public String getModId()
	{
		return owner.getModId();
	}
	
	public String getDefinition()
	{
		return getModId() + ":" + className;
	}
	
	@Nullable
	public InputStream openResource(String relativePath) throws IOException
	{
		return owner.getOwningFile().getFile().getContents()
			.openFile(relativePath);
	}
	
	private static FabricClientGameTest newInstance(IModInfo owner,
		String className)
	{
		try
		{
			return Class.forName(className)
				.asSubclass(FabricClientGameTest.class).getConstructor()
				.newInstance();
		}catch(ClassCastException e)
		{
			throw new IllegalArgumentException(
				"Client gametest class %s from mod %s does not implement %s"
					.formatted(className, owner.getModId(),
						FabricClientGameTest.class.getName()),
				e);
		}catch(ReflectiveOperationException e)
		{
			throw new RuntimeException(
				"Failed to create client gametest %s from mod %s"
					.formatted(className, owner.getModId()),
				e);
		}
	}
}
