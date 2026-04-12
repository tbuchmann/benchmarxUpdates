# BenchmarxFamiliesToPersons – Test Suite Documentation

## Overview

This project contains the Benchmarx test suite for the **Families-to-Persons** bidirectional transformation (BX) benchmark.
The transformation synchronises a *source* model (a `FamilyRegister` containing families and their members) with a *target* model (a `PersonRegister` containing persons with full names and optional birthdays).

The central mapping rule is:

> Every family member `<firstName>` in a family named `<familyName>` corresponds to a person whose full name is `<familyName>, <firstName>`.
> The role of a family member (father / mother / son / daughter) determines the gender of the corresponding person; the parent/child distinction is otherwise **lost** in the Persons model and must be recovered via configuration decisions when synchronising backwards.

### Transformation Decisions

Because the backward direction (Persons → Families) is ambiguous, two boolean configuration decisions are exposed:

| Decision constant | `true` | `false` |
|---|---|---|
| `PREFER_EXISTING_FAMILY_TO_NEW` | Place a new person into an already-existing family with a matching surname | Create a new family for the person |
| `PREFER_CREATING_PARENT_TO_CHILD` | Create the person as a parent (father/mother) in the chosen family | Create the person as a child (son/daughter) in the chosen family |

These decisions can be set (or changed) between individual propagation steps, enabling both fixed-configuration and dynamic-configuration scenarios.

### Tools Under Test

The test suite is parameterised over BX tool implementations. The currently active tools are:

* **BXtendFamiliesToPersons** – BXtend-based implementation (no known failures)
* **WrapperOverBXtendWithMerge** – BXtend with an additional merge layer (no known failures)
* **BXtendDSLFamiliesToPersons** – BXtend DSL-based implementation

Additional implementations are present in the source but commented out (e.g. BiGUL, NMF, eMoflon, MediniQVT, ENeo, IBeXTGG, JTL, BXAgent, Java hand-written).

---

## Test Suite Structure

```
testsuite/
├── batch/
│   ├── fwd/    BatchForward
│   └── bwd/    BatchBwdEAndP
│               BatchBwdENotP
│               BatchBwdNotEAndP
│               BatchBwdNotENotP
├── alignment_based/
│   ├── fwd/    IncrementalForward
│   ├── bwd/    IncrementalBackward
│   └── roundtrip/  RoundtripTests
└── concurrent/
    ├── MonotonicCreating
    ├── MonotonicDeleting
    ├── NonMonotonic
    └── Conflicts
```

---

## 1. Batch Forward Tests (`batch/fwd/BatchForward`)

Batch forward tests apply a single propagation from the **Families (source)** side with no prior state.

### `testInitialiseSynchronisation`
**Precondition:** none (empty source and target registers)  
**Action:** no edit is performed  
**Postcondition:** `RootElementFamilies` / `RootElementPersons`  
**What is tested:** Starting a synchronisation dialogue produces empty but well-formed root elements on both sides.

### `testFamilyNameChangeOfEmpty`
**Precondition:** An empty Simpson family exists (`Pre_NameChangeFamilyEmpty` / `Pre_NameChangePersonEmpty`)  
**Action (fwd):** Rename the empty Simpson family to "Bouvier"  
**Postcondition:** `NameChangeFamilyEmpty` / `NameChangePersonEmpty`  
**What is tested:** Renaming a family that has no members is propagated correctly to the Persons side (no persons are affected, but the family name is updated).

### `testCreateFamily`
**Precondition:** none  
**Action (fwd):** Create the Skinner family (no members)  
**Postcondition:** `OneFamily` / `PersonsForOneFamily`  
**What is tested:** Creating a family without members produces a family register with one entry and an empty person register.

### `testCreateFamilyMember`
**Precondition:** none  
**Action (fwd):** Create the Flanders family and add Rod as a son  
**Postcondition:** `OneFamilyWithOneFamilyMemberSon` / `PersonOneMaleMember`  
**What is tested:** A single family member (son) is correctly translated to a male person with the composite full name.

### `testNewFamilyWithMultiMembers`
**Precondition:** none  
**Action (fwd):** Create the Flanders family with son Rod, then create the full Simpson family with multiple members  
**Postcondition:** `NewFamilyWithMembers` / `PersonsMulti`  
**What is tested:** Multiple family members across two families are all correctly propagated to individual person entries.

### `testNewDuplicateFamilyNames`
**Precondition:** none  
**Action (fwd):** Create a Simpson family with members, then create a *second* empty Simpson family and add father Bart to it  
**Postcondition:** `FamiliesWithSameName` / `PersonWithSameName`  
**What is tested:** Two distinct families sharing the same surname produce persons with the same full name; both entries must be represented.

### `testDuplicateFamilyMemberNames`
**Precondition:** none  
**Action (fwd):** Create the full Simpson family then add a second son named Bart to the same family  
**Postcondition:** `FamilyWithDuplicateMember` / `PersonWithSameName`  
**What is tested:** Duplicate first names within one family (two sons called "Bart") produce two persons with identical full names.

---

## 2. Batch Backward Tests (`batch/bwd/`)

All batch backward tests start from an **empty** source state (no precondition) and propagate a single target (Persons) edit to the Families side. The four test classes cover all four combinations of the two boolean decisions.

### 2.1 `BatchBwdEAndP` — Prefer Existing Family = **true**, Prefer Parent = **true**

#### `testCreateMalePersonAsSon`
**Action (bwd):** Create male person "Rod"  
**Postcondition:** `OneFamilyWithOneFamilyMember` / `PersonOneMaleMember`  
**What is tested:** With `PREFER_EXISTING_FAMILY_TO_NEW = true` and `PREFER_CREATING_PARENT_TO_CHILD = true`, creating a single male person places him as a **father** in a (newly created, since no family exists yet) family. Because no existing family is available, a new one is created; the parent preference makes him a father.

#### `testCreateFamilyMembersInExistingFamilyAsParents`
**Action (bwd):** Create persons Rod (male), Homer (male), Marge (female) in one step  
**Postcondition:** `FamilyWithParentsOnly` / `PersonsMultiDeterministic`  
**What is tested:** Multiple persons created together are placed as parents in a single family when both decisions prefer existing family and parent role. With `PREFER_EXISTING_FAMILY_TO_NEW = true`, each successive person is inserted into the family just created by the previous person. With `PREFER_CREATING_PARENT_TO_CHILD = true`, all are placed as father/mother.

---

### 2.2 `BatchBwdENotP` — Prefer Existing Family = **true**, Prefer Parent = **false**

#### `testCreateMalePersonAsSon`
**Action (bwd):** Create male person "Rod"  
**Postcondition:** `OneFamilyWithOneFamilyMemberSon` / `PersonOneMaleMember`  
**What is tested:** Same single-person scenario as above but with `PREFER_CREATING_PARENT_TO_CHILD = false`: the male person is placed as a **son** instead of a father.

#### `testCreateFamilyMembersInExistingFamilyAsChildren`
**Action (bwd):** Create persons Rod, Homer, Bart, Marge, Lisa, Maggie  
**Postcondition:** `FamiliesWithChildrenOnly` / `PersonsMulti`  
**What is tested:** All persons are created as **children** (sons/daughters) inside one family, because the child preference is active and successive persons reuse the existing family.

#### `testCreateDuplicateFamilyMembersInExistingFamilyAsChildren`
**Action (bwd):** Create persons Rod, Bart, Homer, Bart, Bart, Marge, Lisa, Maggie (Bart appears three times)  
**Postcondition:** `FamilyWithDuplicateChildrenOnly` / `PersonsDuplicateMulti`  
**What is tested:** Persons with duplicate names are all inserted into the existing family as children, producing multiple family member entries with the same first name.

---

### 2.3 `BatchBwdNotEAndP` — Prefer Existing Family = **false**, Prefer Parent = **true**

#### `testCreateMalePersonAsParent`
**Action (bwd):** Create male person "Rod"  
**Postcondition:** `OneFamilyWithOneFamilyMember` / `PersonOneMaleMember`  
**What is tested:** With `PREFER_EXISTING_FAMILY_TO_NEW = false`, a new family is always created. With `PREFER_CREATING_PARENT_TO_CHILD = true`, the person becomes a **father**.

#### `testCreateFamilyMembersInNewFamilyAsParents`
**Action (bwd):** Create persons Rod, Homer, Bart, Marge, Lisa, Maggie  
**Postcondition:** `MultiFamiliesParents` / `PersonsMulti`  
**What is tested:** Because a new family is preferred for each person, each person with a distinct surname triggers a new family, all members being placed as parents. The result is multiple families each with one parent.

#### `testCreateDuplicateFamilyMembersInNewFamilyAsParents`
**Action (bwd):** Create persons Rod, Bart, Homer, Bart, Bart, Marge, Lisa, Maggie (Bart three times)  
**Postcondition:** `MultiFamiliesWithDuplicateNamesParents` / `PersonsDuplicateMulti`  
**What is tested:** Each person (including duplicates) triggers a new family, resulting in multiple families each containing one parent; duplicate-named persons end up in separate families.

---

### 2.4 `BatchBwdNotENotP` — Prefer Existing Family = **false**, Prefer Parent = **false**

#### `testCreateMalePersonAsSon`
**Action (bwd):** Create male person "Rod"  
**Postcondition:** `OneFamilyWithOneFamilyMemberSon` / `PersonOneMaleMember`  
**What is tested:** New family is created and person is placed as a **son** (not a father).

#### `testCreateFamilyMembersInNewFamilyAsChildren`
**Action (bwd):** Create persons Rod, Homer, Bart, Marge, Lisa, Maggie  
**Postcondition:** `MultiFamiliesChildren` / `PersonsMulti`  
**What is tested:** Each person gets a new family; all are placed as children (sons/daughters).

#### `testCreateDuplicateFamilyMembersInNewFamilyAsChildren`
**Action (bwd):** Create persons Rod, Bart, Homer, Bart, Bart, Marge, Lisa, Maggie  
**Postcondition:** `MultiFamiliesWithDuplicateNamesChildren` / `PersonsDuplicateMulti`  
**What is tested:** Duplicate persons each get their own new family; all placed as children.

---

## 3. Incremental Forward Tests (`alignment_based/fwd/IncrementalForward`)

Incremental forward tests start from a common, non-trivial precondition state (`Pre_IncrFwdFamily` / `Pre_IncrFwdPerson`) built up by a shared setup sequence:

1. Create Skinner family, Flanders family, son Rod in Flanders, Simpson family, father Bart in Simpson  
2. (idle target) set birthday of Rod  
3. (idle target) set birthday of father Bart  
4. Create full Simpson family with all members  
5. (idle target) change all birthdays  
6. Create son Bart (second Bart in Simpsons)  
7. (idle target) set birthday of younger Bart  

"Idle" target edits (e.g. setting birthdays) are applied to the target without being propagated, so they contribute to the alignment state but do not drive synchronisation.

### `testIncrementalInserts`
**Additional action (fwd):** Add father Ned, mother Maude, and son Todd to the Flanders family  
**Postcondition:** `FamilyAfterInsertion` / `PersonAfterInsertion`  
**What is tested:** Incrementally adding new family members (parent and child roles, both genders) to an existing family propagates the correct new person entries while preserving all existing persons and their birthdays.

### `testIncrementalDeletions`
**Setup variation:** Common precondition is `Pre_IncrFwdFamilyForDeletion` / `Pre_IncrFwdPersonForDeletion` (slightly different; a Bart son is added then immediately deleted)  
**Additional actions (fwd):**  
1. Create son Bart  
2. Delete the first son Bart  
**Postcondition:** `FamilyAfterDeletion` / `PersonAfterDeletion`  
**What is tested:** Adding and then removing a family member propagates the deletion to the Persons side correctly, resulting in the state as if the member had never been added.

### `testIncrementalRename`
**Additional action (fwd):** Rename the Simpson family to "Bouvier"  
**Postcondition:** `FamilyAfterRename` / `PersonAfterRename`  
**What is tested:** Renaming a family propagates to all corresponding persons whose full name contains the old surname; all affected persons receive the new family name as their surname.

### `testIncrementalMove`
**Additional actions (fwd):** Move Lisa and move Marge (both to different families/roles)  
**Postcondition:** `FamilyAfterMove` / `PersonAfterMove`  
**What is tested:** Moving family members between families (role-preserving) is propagated correctly: persons retain their names but the family affiliation changes accordingly.

### `testIncrementalMixed`
**Additional actions (fwd):**  
1. Delete father Homer  
2. Re-create father Homer  
**Postcondition:** `FamilyAfterMixed` / `PersonAfterMixed`  
**What is tested:** A combined delete-then-recreate sequence for a family member propagates as if the member was replaced; the corresponding person is deleted and a new one created (without birthday, since the birthday was on the deleted person).

### `testIncrementalMoveRoleChange`
**Additional action (fwd):** Move Maggie and change her role in the destination family  
**Postcondition:** `FamilyAfterMoveRoleChange` / `PersonAfterMoveRoleChange`  
**What is tested:** Moving a family member while simultaneously changing her role (e.g. from daughter to mother) propagates correctly; the gender mapping may change with the role.

### `testStability`
**Precondition:** none (creates a fresh Simpson family with duplicate son Bart)  
**First postcondition:** `FamilyWithDuplicateMember` / `PersonWithSameName`  
**Action:** Apply an **idle delta** (a no-op edit) to the source  
**Second postcondition:** `FamilyWithDuplicateMember` / `PersonWithSameName` (unchanged)  
**What is tested:** **Stability** — propagating an edit that changes nothing leaves the target completely unchanged.

### `testHippocraticness`
**Precondition:** none (creates a fresh Simpson family with duplicate son Bart)  
**First postcondition:** `FamilyWithDuplicateMember` / `PersonWithSameName`  
**Action:** Apply a **hippocratic delta** — a source edit that does not change the observable model state (e.g. changes an attribute to the same value)  
**Second postcondition:** `FamilyWithDuplicateMember2` / `PersonWithSameName` (families side unchanged; persons side should still match)  
**What is tested:** **Hippocraticness** — a source edit that produces no net change to the source model must not change the target model.

---

## 4. Incremental Backward Tests (`alignment_based/bwd/IncrementalBackward`)

Incremental backward tests propagate edits from the **Persons (target)** side. The decisions can be changed between steps to test dynamic configuration.

### `testIncrementalInsertsFixedConfig`
**Configuration:** `PREFER_EXISTING_FAMILY_TO_NEW = true`, `PREFER_CREATING_PARENT_TO_CHILD = true` (fixed throughout)  
**Setup:** Create Homer and Maggie as parents; set birthdays of Simpson family  
**Precondition:** `Pre_IncrBwdFamily` / `Pre_IncrBwdPerson`  
**Actions:**  
1. Create Seymour → `FamilyAfterBwdInsertion1` / `PersonAfterBwdInsertion1`  
2. Create Seymour again → `FamilyAfterBwdInsertion2` / `PersonAfterBwdInsertion2`  
**What is tested:** Inserting a new person twice (same name) with fixed decisions: both insertions go to the existing family as parents, producing two family members with the same first name.

### `testIncrementalInsertsDynamicConfig`
**Configuration:** Changes between steps (see below)  
**Setup:** Same as `testIncrementalInsertsFixedConfig`  
**Precondition:** `Pre_IncrBwdFamily` / `Pre_IncrBwdPerson`  
**Actions and configuration changes:**

| Step | Config (E=prefer existing, P=prefer parent) | Action | Postcondition |
|---|---|---|---|
| 1 | E=true, P=true | Create Seymour | `FamilyAfterBwdInsertion1` |
| 2 | E=true, P=true | Create Seymour | `FamilyAfterBwdInsertion2` |
| 3 | E=false, P=true | Create Seymour | `FamilyAfterBwdInsertion3` |
| 4 | E=false, P=false | Create Seymour | `FamilyAfterBwdInsertion4` |
| 5 | E=true, P=true | Create Seymour | `FamilyAfterBwdInsertion5` |
| 6 | E=true, P=false | Create Bart and Lisa | `FamilyAfterBwdInsertion6` |

**What is tested:** Changing decisions between insertions places persons in different families (new vs. existing) or different roles (parent vs. child), demonstrating that the configuration drives placement dynamically.

### `testIncrementalDeletions`
**Configuration:** First Homer created as parent (P=true); then Maggie created as child (P=false); both with E=true  
**Precondition:** `Pre_IncrBwdFamilyFatherChild` / `Pre_IncrBwdPerson`  
**Actions:**  
1. Delete Homer  
2. Delete Maggie  
**Postcondition:** `FamilyAfterBwdDeletion` / `PersonAfterBwdDeletion`  
**What is tested:** Deleting persons backward propagates to delete the corresponding family members. After deleting both the father and a child, the family is left empty (or removed).

### `testIncrementalRenamingDynamic`
**Configuration:** Multiple configuration changes across a long build-up sequence  
**Setup:** A complex multi-step sequence building a state with Rod (Flanders, son), Homer (new family, father), Marge+Bart (existing, parents), Lisa+Maggie (existing, parents), a second Bart (existing, son), a father Bart (new family, father)  
**Precondition:** `Pre_IncrBwdFamilyRenameDynamic` / `Pre_IncrBwdPersonRenameDynamic`  
**Renaming actions:**

| Config | Action |
|---|---|
| — | First-name change of Bart (one instance) |
| E=true, P=true | Full name change of the other Bart |
| E=true, P=false | Full name change of father Bart |
| E=false, P=false | Family name change of Lisa |
| E=false, P=true | Full name change of Marge |

**Postcondition:** `FamilyAfterBwdIncrRenameDynamic` / `PersonAfterBwdIncrRenameDynamic`  
**What is tested:** A variety of renaming operations on the Persons side (first-name only, full name, family name only) propagate correctly to the Families side: first-name changes update the family member's first name; family-name changes require moving a member to a (new or existing) differently-named family.

### `testIncrementalMixedDynamic`
**Configuration:** Setup with E=true, P=true; action step with E=true, P=false  
**Setup:** Create Maggie (parent) and Homer (parent); set birthdays  
**Precondition:** `Pre_IncrBwdFamily` / `Pre_IncrBwdPerson`  
**Action:** Delete Homer and immediately re-create Homer in the same step  
**Postcondition:** `FamilyAfterBwdMixed` / `PersonAfterBwdMixed`  
**What is tested:** A combined delete-and-create for the same person name, with P=false, places the re-created Homer as a **child** even though the original was a father; the resulting family state reflects the new role.

### `testIncrementalOperational`
**Setup:** Create Maggie as child; (idle) set birthday of Maggie  
**Precondition:** `Pre_IncrBwdOpFamily` / `Pre_IncrBwdOpPerson`  
**Actions:**  
1. (E=true, P=true) Create Marge, Lisa, Homer, Bart, Maggie (duplicate), Lisa (duplicate) — in one step  
2. (E=false, P=true) Create Lisa (third time)  
**Postcondition:** `FamilyAfterIncrOp` / `PersonAfterIncrOp`  
**What is tested:** A complex mix of insertions including duplicates, with a mid-sequence decision change, verifies that the tool correctly accumulates state across many propagation steps and handles duplicate names and role assignments.

### `testStability`
**Configuration:** E=true, P=true  
**Action 1:** Create Rod, Homer, Marge → `FamilyWithParentsOnly` / `PersonsMultiDeterministic`  
**Action 2:** Apply idle delta  
**Postcondition:** `FamilyWithParentsOnly` / `PersonsMultiDeterministic` (unchanged)  
**What is tested:** **Stability** (backward direction) — a no-op propagation leaves both models unchanged.

### `testHippocraticness`
**Configuration:** E=true, P=true  
**Action 1:** Create Rod, Homer, Marge → `FamilyWithParentsOnly` / `PersonsMultiDeterministic`  
**Action 2:** Apply hippocratic delta (no net change to Person model)  
**Postcondition:** `FamilyWithParentsOnly` / `PersonsMultiDeterministic2`  
**What is tested:** **Hippocraticness** (backward direction) — a target edit that does not change the observable state of the Persons model must not change the Families model.

---

## 5. Round-Trip Tests (`alignment_based/roundtrip/RoundtripTests`)

Round-trip tests interleave forward and backward propagation steps, verifying that sequential propagations in alternating directions maintain overall consistency.

All three tests share the same setup sequence leading to `Pre_IncrFwdFamily` / `Pre_IncrFwdPerson`.

### `testRoundtripEdit`
**Actions:**  
1. (bwd) Change first name of Homer in the Persons model  
2. (fwd) Rename the Flanders family to "Bouvier"  
**Postcondition:** `FamilyAfterRoundtripEdit` / `PersonAfterRoundtripEdit`  
**What is tested:** A backward rename followed by a forward rename converges to a consistent state where both the family members and the persons register reflect both changes without interference.

### `testRoundtripAdd`
**Configuration:** E=true, P=false  
**Actions:**  
1. (bwd) Create Seymour as a new person  
2. (fwd) Create son Todd in the source  
**Postcondition:** `FamilyAfterRoundtripAdd` / `PersonAfterRoundtripAdd`  
**What is tested:** Adding an element backward (a new person) and then adding an element forward (a new family member) are both reflected correctly in the final consistent state.

### `testRoundtripDelete`
**Actions:**  
1. (bwd) Delete Marge from the Persons model  
2. (fwd) Delete Rod as a son from the Families model  
**Postcondition:** `FamilyAfterRoundtripDelete` / `PersonAfterRoundtripDelete`  
**What is tested:** Deleting an element backward and a different element forward both propagate without interfering with each other; the final state is consistent with both deletions applied.

---

## 6. Concurrent Synchronisation Tests — Monotonic Creating (`concurrent/MonotonicCreating`)

Concurrent tests apply edits to **both** source and target simultaneously and verify that the tool can merge them consistently. "Monotonic creating" tests cover scenarios where both sides only add elements (no deletions).

### `testSuitableFamilyNonMatchingMember`
**Precondition:** none  
**Concurrent edit:**  
- Source: create Simpson family + father Homer  
- Target: create Bart (male person) + set all birthdays  
**Postcondition:** `FamilyAfterBasicConcurrentEdit1` / `PersonsAfterBasicConcurrentEdit1`  
**What is tested:** Source creates Homer as father; target creates Bart independently. Because Bart's surname matches Simpson but the existing member is Homer (not Bart), Bart must be placed as a **new** element — the source's new family member and the target's new person are non-matching. The merged result must contain both Homer and Bart in the Simpson family.

### `testSuitableFamilyMatchingMember`
**Configuration:** E=true, P=true  
**Precondition:** none  
**Concurrent edit:**  
- Source: create Simpson family + father Homer  
- Target: create Homer (male person) + set all birthdays  
**Postcondition:** `FamilyAfterBasicConcurrentEdit2` / `PersonsAfterBasicConcurrentEdit2`  
**What is tested:** Both sides independently create the same person (Homer Simpson). The tool must recognise that these two independent creations correspond and merge them into a single family member / person pair rather than duplicating.

### `testNonSuitableFamily`
**Configuration:** E=true, P=true  
**Precondition:** none  
**Concurrent edit:**  
- Source: create Simpson family + father Homer  
- Target: create Seymour (male, different surname) + set Seymour's birthday  
**Postcondition:** `FamilyAfterBasicConcurrentEdit3` / `PersonsAfterBasicConcurrentEdit3`  
**What is tested:** The target creates a person (Seymour Skinner) whose surname does not match the family created by the source (Simpson). The tool must create a separate (new or existing) family for Seymour while keeping Homer in the Simpson family.

### `testCombinedCases`
**Configuration:** E=true, P=false  
**Precondition:** none  
**Concurrent edit:**  
- Source: create Simpson family, father Homer, son Bart  
- Target: create Homer, create Seymour, set all birthdays, set Seymour's birthday  
**Postcondition:** one of `FamilyAfterBasicConcurrentEdit4` / `PersonsAfterBasicConcurrentEdit4` **or** `FamilyAfterBasicConcurrentEdit5` / `PersonsAfterBasicConcurrentEdit5`  
**What is tested:** A combination of matching (Homer) and non-matching (Seymour) concurrent creations, plus Bart being added only on the source. Tools may legitimately produce one of two valid outcomes (hence `assertAnyPostcondition`), reflecting permitted non-determinism in how the merge resolves the combined scenario.

---

## 7. Concurrent Synchronisation Tests — Monotonic Deleting (`concurrent/MonotonicDeleting`)

All three tests share the same long setup sequence leading to `Pre_IncrFwdFamily` / `Pre_IncrFwdPerson`.

### `testMatchingDeletion`
**Concurrent edit:**  
- Source: delete father Homer  
- Target: delete Homer (person)  
**Postcondition:** `FamiliesAfterConcSyncMatchingDeletion` / `PersonsAfterConcSyncMatchingDeletion`  
**What is tested:** Both sides independently delete the **same** element (Homer). The merged result must be consistent: Homer is gone from both models; no duplication of the deletion or spurious side-effects.

### `testNonMatchingDeletion`
**Concurrent edit:**  
- Source: delete father Homer  
- Target: delete Maggie (a different person)  
**Postcondition:** `FamiliesAfterConcSyncNonMatchingDeletion` / `PersonsAfterConcSyncNonMatchingDeletion`  
**What is tested:** Both sides delete **different** elements concurrently. Both deletions must be respected and propagated: Homer is deleted from the Families side (and his person entry removed from Persons), and Maggie is deleted from the Persons side (and her family member entry removed from Families).

### `testCombinedCases`
**Concurrent edit:**  
- Source: delete father Homer **and** delete Rod as son  
- Target: delete Homer **and** delete Maggie  
**Postcondition:** `FamiliesAfterConcSyncCombinedCases` / `PersonsAfterConcSyncCombinedCases`  
**What is tested:** Multiple concurrent deletions on both sides, mixing matching deletions (Homer on both sides) and non-matching deletions (Rod on source, Maggie on target). All four deletions must be reflected correctly in the final consistent state.

---

## 8. Concurrent Synchronisation Tests — Non-Monotonic (`concurrent/NonMonotonic`)

Non-monotonic concurrent tests involve concurrent edits where at least one side includes **deletions** while the other includes **creations**, or other combinations that are not purely additive.

Both tests share the same long setup sequence leading to `Pre_IncrFwdFamily` / `Pre_IncrFwdPerson`.

### `testCombinedDeletionAndCreation`
**Concurrent edit:**  
- Source: delete father Homer **and** create father Ned (in Flanders)  
- Target: delete Marge **and** delete Homer  
**Postcondition:** `FamiliesAfterConcSyncCombinedNonMonotonicCases` / `PersonsAfterConcSyncCombinedNonMonotonicCases`  
**What is tested:** The source simultaneously removes one element and adds another while the target performs two deletions (one matching the source deletion, one independent). The tool must correctly merge: Homer is gone (matched deletion), Marge is gone (target-only deletion propagated to source), and Ned is a new family member (source-only creation propagated to target).

### `testCombinedRenameDelete`
**Concurrent edit:**  
- Source: delete father Homer  
- Target: rename Lisa (first-name change)  
**Postcondition:** `FamiliesAfterConcSyncCombinedNonMonotonicRenameDelete` / `PersonsAfterConcSyncCombinedNonMonotonicRenameDelete`  
**What is tested:** A concurrent source deletion (Homer) and target rename (Lisa) must both be propagated without interference: Homer is removed on both sides, and Lisa's rename is reflected in the Families model.

---

## 9. Concurrent Synchronisation Tests — Conflicts (`concurrent/Conflicts`)

Conflict tests cover scenarios where concurrent edits on the two sides are **semantically incompatible**. The tool must detect the conflict and resolve it in one of several valid ways. Each test uses `assertAnyPostcondition` to allow any one of the specified valid resolutions.

All four tests share the same setup: create full Simpson and Flanders families, then set Lisa's birthday. Precondition: `Pre_ConflictFamily` / `Pre_ConflictPersons`.

### `testMoveDeleteConflict`
**Concurrent edit:**  
- Source: move Lisa from Simpson to Flanders as a daughter  
- Target: delete Lisa  
**Expected (any of three):**

| Option | Description | States |
|---|---|---|
| 1 | Move wins: Lisa appears in Flanders; her old Simpson entry is removed | `Post_MoveDeleteConflictFamily_1` / `Post_MoveDeleteConflictPersons_1` |
| 2 | Delete wins: Lisa is deleted; move is rejected | `Post_MoveDeleteConflictFamily_2` / `Post_MoveDeleteConflictPersons_2` |
| 3 | Move wins, delete is rejected | `Post_MoveDeleteConflictFamily_3` / `Post_MoveDeleteConflictPersons_3` |

**What is tested:** Resolution of a **move/delete conflict** — when one side moves an element and the other deletes it simultaneously, the tool must choose a consistent resolution.

### `testMoveRenameConflict`
**Configuration:** E=false, P=false  
**Concurrent edit:**  
- Source: move Lisa from Simpson to Flanders as a daughter  
- Target: change Lisa's family name to "Van Houten"  
**Expected (any of two):**

| Option | Description | States |
|---|---|---|
| 1 | Rename wins: Lisa's surname is changed to Van Houten; move is rejected | `Post_MoveRenameConflictFamily_1` / `Post_MoveRenameConflictPersons_1` |
| 2 | Move wins: Lisa ends up in Flanders; family name rename is rejected | `Post_MoveRenameConflictFamily_2` / `Post_MoveRenameConflictPersons_2` |

**What is tested:** Resolution of a **move/rename conflict** — moving a family member (source) conflicts with renaming the corresponding person's family name (target).

### `testDeleteRenameConflict`
**Configuration:** E=true, P=false  
**Concurrent edit:**  
- Source: delete Lisa from the Simpson family  
- Target: rename Lisa (first-name change)  
**Expected (any of two):**

| Option | Description | States |
|---|---|---|
| 1 | Rename wins: deletion is rejected; Lisa's name is updated | `Post_DeleteRenameConflictFamily_1` / `Post_DeleteRenameConflictPersons_1` |
| 2 | Delete wins: Lisa is deleted; rename is discarded | `Post_DeleteRenameConflictFamily_2` / `Post_DeleteRenameConflictPersons_2` |

**What is tested:** Resolution of a **delete/rename conflict** — deleting a family member on the source side conflicts with renaming the corresponding person on the target side.

### `testRenameRenameConflict`
**Concurrent edit:**  
- Source: rename Lisa (change first name in Families)  
- Target: rename Lisa (change first name in Persons)  
**Expected (any of three):**

| Option | Description | States |
|---|---|---|
| 1 | Source rename wins | `Post_RenameRenameConflictFamily_1` / `Post_RenameRenameConflictPersons_1` |
| 2 | Target rename wins | `Post_RenameRenameConflictFamily_2` / `Post_RenameRenameConflictPersons_2` |
| 3 | Both renamings are kept (propagated to the respective other side) | `Post_RenameRenameConflictFamily_3` / `Post_RenameRenameConflictPersons_3` |

**What is tested:** Resolution of a **rename/rename conflict** — both sides independently rename the same element. Three different valid resolution strategies are accepted: source wins, target wins, or both changes are accepted (requiring a merge of two different rename operations onto the same element).

---

## Summary Table

| Class | # Tests | Direction | Type |
|---|---|---|---|
| `BatchForward` | 6 | Fwd | Batch |
| `BatchBwdEAndP` | 2 | Bwd | Batch (E=true, P=true) |
| `BatchBwdENotP` | 3 | Bwd | Batch (E=true, P=false) |
| `BatchBwdNotEAndP` | 3 | Bwd | Batch (E=false, P=true) |
| `BatchBwdNotENotP` | 3 | Bwd | Batch (E=false, P=false) |
| `IncrementalForward` | 8 | Fwd | Incremental (alignment-based) |
| `IncrementalBackward` | 8 | Bwd | Incremental (alignment-based) |
| `RoundtripTests` | 3 | Fwd+Bwd | Round-trip |
| `MonotonicCreating` | 4 | Concurrent | Monotonic (creates only) |
| `MonotonicDeleting` | 3 | Concurrent | Monotonic (deletes only) |
| `NonMonotonic` | 2 | Concurrent | Non-monotonic |
| `Conflicts` | 4 | Concurrent | Conflict resolution |
| **Total** | **49** | | |
