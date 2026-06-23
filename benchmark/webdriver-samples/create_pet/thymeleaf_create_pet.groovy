import org.openqa.selenium.By
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

def wait = new WebDriverWait(WDS.browser, Duration.ofSeconds(100))
def petName = 'Fluffy-' + System.currentTimeMillis()
def ownerId = WDS.vars.get('OWNER_ID')
def port = WDS.vars.get('THYMELEAF_PORT') ?: '8080'

// 1. Load the owner details page (full page load)
WDS.browser.get("http://localhost:${port}/owners/" + ownerId)
wait.until({ d ->
    try {
        d.findElement(By.xpath("//h2[text()='Owner Information']")).isDisplayed()
    } catch (e) { false }
})

// 2. Click "Add New Pet" link to navigate to the form (full page load)
WDS.browser.findElement(By.xpath("//a[contains(normalize-space(), 'Add New Pet')]")).click()
wait.until({ d ->
    try {
        d.findElement(By.cssSelector("input[name='name']")).isDisplayed()
    } catch (e) { false }
})

// 3. Fill form
def nameInput = WDS.browser.findElement(By.cssSelector("input[name='name']"))
nameInput.clear()
nameInput.sendKeys(petName)

def birthDateInput = WDS.browser.findElement(By.cssSelector("input[name='birthDate']"))
birthDateInput.clear()
birthDateInput.sendKeys('2020-01-15')

def typeSelect = new Select(WDS.browser.findElement(By.cssSelector("select[name='type']")))
typeSelect.selectByValue('cat')

// 4. Submit form via submit button click (waits for navigation)
WDS.browser.findElement(By.cssSelector("button[type='submit']")).click()

// 5. Wait for redirect to owner details page (full page reload)
wait.until({ d ->
    !d.getCurrentUrl().contains('/pets/new')
})

// 6. Wait for the new pet name to appear in a <dd> on the owner details page
wait.until({ d ->
    try {
        d.findElement(By.xpath("//dd[text()='" + petName + "']")).isDisplayed()
    } catch (e) { false }
})

WDS.sampleResult.setResponseData("Pet created via full roundtrip: " + petName)
WDS.sampleResult.setDataType(org.apache.jmeter.samplers.SampleResult.TEXT)
WDS.sampleResult.setSuccessful(true)
