package org.springframework.samples.petclinic.views.visits

import htmlflow.tr
import htmlflow.view
import org.springframework.samples.petclinic.Routes
import org.springframework.samples.petclinic.owner.Owner
import org.springframework.samples.petclinic.pet.Pet
import org.xmlet.htmlapifaster.Table
import org.xmlet.htmlapifaster.Tr
import org.xmlet.htmlapifaster.a
import org.xmlet.htmlapifaster.button
import org.xmlet.htmlapifaster.i
import org.xmlet.htmlapifaster.tbody
import org.xmlet.htmlapifaster.td
import org.xmlet.htmlapifaster.th
import org.xmlet.htmlapifaster.thead
import org.xmlet.htmlapifaster.tr
import org.xmlet.htmlflow.datastar.Signal
import org.xmlet.htmlflow.datastar.attributes.dataAttr
import org.xmlet.htmlflow.datastar.attributes.dataIndicator
import org.xmlet.htmlflow.datastar.attributes.dataOn
import org.xmlet.htmlflow.datastar.attributes.dataSignal
import org.xmlet.htmlflow.datastar.events.Click

// Used in OwnersDetails
internal fun Table<*>.visitsTableHead() {
    thead {
        tr {
            th { text("Visit Date") }
            th { text("Description") }
        }
    }
}

internal fun Table<*>.visitsTableBody(
    owner: Owner,
    pet: Pet,
) {
    tbody.of { innerbody ->
        pet.getVisits().forEach { vs ->
            innerbody.tr {
                td { text(vs.date) }
                td { text(vs.description) }
            }
        }
        tr {
            attrId("edit-pet-add-visit-buttons-${pet.id}")
            editPetAddVisitButtons(pet, owner, false)
        }
    }
}
fun Tr<*>.editPetAddVisitButtons(pet: Pet, owner: Owner, disabled: Boolean){
    attrId("edit-pet-add-visit-buttons-${pet.id}")
    td {
        button {
            attrId("pet-edit")
            attrClass("btn btn-primary")
            attrDisabled(disabled)
            dataOn(Click) {
                get(Routes.petEdit(owner.id, pet.id))
            }
            val fetching = dataIndicator("_fetching")
            dataAttr("disabled") { +"$disabled || $fetching" }
            i { attrClass("pixelarticons:close") }
            text("Edit Pet")
        }
    }
    td {
        a {
            attrClass("btn btn-primary")
            attrHref(Routes.visitNew(owner.id, pet.id))
            text("Add Visit")
        }
    }
}

fun editPetButtonView(pet: Pet, owner: Owner, disabled: Boolean) = view<Unit> {
    tr { editPetAddVisitButtons(pet, owner, disabled) }
}

