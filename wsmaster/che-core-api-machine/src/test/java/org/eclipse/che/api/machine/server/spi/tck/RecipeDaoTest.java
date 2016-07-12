/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.machine.server.spi.tck;

import com.google.common.collect.ImmutableList;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.machine.server.model.impl.AclEntryImpl;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.machine.server.spi.RecipeDao;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.test.tck.TckModuleFactory;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests {@link RecipeDao} contract.
 *
 * @author Anton Korneta
 */
@Guice(moduleFactory = TckModuleFactory.class)
@Test(suiteName = RecipeDaoTest.SUITE_NAME)
public class RecipeDaoTest {

    public static final String SUITE_NAME = "RecipeDaoTck";

    private static final int ENTRY_COUNT = 5;

    private List<RecipeImpl> recipes;

    @Inject
    private RecipeDao recipeDao;

    @Inject
    private TckRepository<RecipeImpl> tckRepository;

    @BeforeMethod
    public void setUp() throws Exception {
        recipes = new ArrayList<>(5);
        for (int i = 0; i < ENTRY_COUNT; i++) {
            recipes.add(createRecipe(i));
        }
        RecipeImpl recipe = createRecipe(10);
        ArrayList<AclEntryImpl> arrayList = new ArrayList<>();
        arrayList.add(new AclEntryImpl("*", ImmutableList.of("search", "read")));
        recipe.setAcl(arrayList);
        recipes.add(recipe);
        tckRepository.createAll(recipes);
    }

    @AfterMethod
    public void cleanUp() throws Exception {
        tckRepository.removeAll();
    }

    @Test(dependsOnMethods = "shouldGetRecipeById")
    public void shouldCreateRecipe() throws Exception {
        final RecipeImpl recipe = createRecipe(0);
        recipeDao.create(recipe);

        assertEquals(recipeDao.getById(recipe.getId()), recipe);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenCreateNullRecipe() throws Exception {
        recipeDao.create(null);
    }

    @Test(expectedExceptions = ConflictException.class)
    public void shouldThrowConflictExceptionWhenCreatingRecipeWithExistingId() throws Exception {
        recipeDao.create(recipes.get(0));
    }

    @Test
    public void shouldUpdateRecipe() throws Exception {
        final RecipeImpl update = recipes.get(0).withName("updatedName");

        assertEquals(recipeDao.update(update), update);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenUpdatingRecipeNull() throws Exception {
        recipeDao.update(null);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenUpdatingNonExistingRecipe() throws Exception {
        recipeDao.update(createRecipe(7));
    }

    @Test(expectedExceptions = NotFoundException.class,
          dependsOnMethods = "shouldThrowNotFoundExceptionWhenGettingNonExistingRecipe")
    public void shouldRemoveRecipe() throws Exception {
        final String existedId = recipes.get(0).getId();

        recipeDao.remove(existedId);
        recipeDao.getById(existedId);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenRemovingRecipeIdNull() throws Exception {
        recipeDao.remove(null);
    }

    @Test
    public void shouldGetRecipeById() throws Exception {
        final RecipeImpl recipe = recipes.get(0);

        assertEquals(recipeDao.getById(recipe.getId()), recipe);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenGettingRecipeIdNull() throws Exception {
        recipeDao.getById(null);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenGettingNonExistingRecipe() throws Exception {
        recipeDao.getById("non-existing");
    }

    @Test
    public void shouldFindRecipeByUser() throws Exception {
        final List<RecipeImpl> result = recipeDao.search(recipes.get(0).getAcl().get(0).getUser(), null, null, 0, recipes.size());

        assertTrue(result.contains(recipes.get(0)));
    }

    @Test(dependsOnMethods = "shouldFindRecipeByUser")
    public void shouldFindingRecipesByTags() throws Exception {
        final RecipeImpl recipe = recipes.get(0);
        final List<RecipeImpl> result = recipeDao.search(recipe.getAcl().get(0).getUser(), recipe.getTags(), null, 0, recipes.size());

        assertTrue(result.contains(recipe));
    }

    @Test(dependsOnMethods = "shouldFindRecipeByUser")
    public void shouldFindRecipeByType() throws Exception {
        final RecipeImpl recipe = recipes.get(0);
        final List<RecipeImpl> result = recipeDao.search(recipe.getAcl().get(0).getUser(), null, recipe.getType(), 0, recipes.size());

        assertTrue(result.contains(recipe));
    }

    @Test(dependsOnMethods = {"shouldFindRecipeByUser", "shouldFindingRecipesByTags", "shouldFindRecipeByType"})
    public void shouldFindRecipeByUserTagsAndType() throws Exception {
        final RecipeImpl recipe = recipes.get(0);
        final List<RecipeImpl> result = recipeDao.search(recipe.getAcl().get(0).getUser(),
                                                         recipe.getTags(),
                                                         recipe.getType(),
                                                         0,
                                                         1);

        assertTrue(result.contains(recipe));
    }

    private static RecipeImpl createRecipe(int index) {
        final String recipeId = NameGenerator.generate("recipeId", 5);
        return new RecipeImpl(recipeId,
                              "recipeName" + index,
                              "creator" + index,
                              "dockerfile" + index,
                              "script",
                              ImmutableList.of("tag1" + index, "tag2" + index),
                              "recipe description",
                              ImmutableList.of(new AclEntryImpl("userId_" + index, ImmutableList.of("read", "search", "update"))));
    }
}
