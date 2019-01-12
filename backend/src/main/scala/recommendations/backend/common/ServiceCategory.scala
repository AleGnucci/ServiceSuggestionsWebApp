package recommendations.backend.common

object ServiceCategory extends Enumeration {
  val PETS: ServiceCategory.Value = Value("Animals and Pets")
  val ART: ServiceCategory.Value = Value("Art")
  val CLOTHES: ServiceCategory.Value = Value("Clothes")
  val IT: ServiceCategory.Value = Value("Information Technology")
  val CRAFTSMAN: ServiceCategory.Value = Value("CraftsMan")
  val ELECTRONICS: ServiceCategory.Value = Value("Electronics")
  val ENTERTAINMENT: ServiceCategory.Value = Value("Entertainment")
  val FOOD: ServiceCategory.Value = Value("Food and Beverage")
  val HEALTH: ServiceCategory.Value = Value("Health")
  val HOTELS: ServiceCategory.Value = Value("Hotels and Campsites")
  val KIDS: ServiceCategory.Value = Value("Kids")
  val LEGAL: ServiceCategory.Value = Value("Legal Services")
  val SPORT: ServiceCategory.Value = Value("Sport")
  val PUBLIC: ServiceCategory.Value = Value("Public Services")
  val TRANSPORTATION: ServiceCategory.Value = Value("Transportation and Travel")
  val FOR_COMPANIES: ServiceCategory.Value = Value("For Companies")
  val PHONE: ServiceCategory.Value = Value("Phones and Internet")
  val MONEY: ServiceCategory.Value = Value("Money and Banks")
  val INSURANCE: ServiceCategory.Value = Value("Insurance")
  val EDUCATION: ServiceCategory.Value = Value("Education")
}
