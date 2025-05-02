program DestructorDemo;

type
  TPerson = class
  private
    FName: string;
    LName: string;
  public
    constructor Create(const AName: string; const BName: string);
    destructor Destroy; override;
    function GetFname: string;
    procedure SetFname(const Value: string);
  end;


constructor TPerson.Create(const AName: string; const BName: string);
begin
  Writeln('Constructor called: Creating a person object');
  FName := AName;
  LName := BName;
end;

destructor TPerson.Destroy;
begin
  Writeln('Destructor called: Cleaning up the object');
  inherited;
end;

function TPerson.GetFname: string;
begin
  Result := FName + ' ' + LName;
end;

procedure TPerson.SetFname(const Value: string);
begin
  FName := Value;
end;

var
  Person: TPerson;
  FirstName: string;
  LastName: string;
  Fullname: string;
begin
Write('Enter First name: ');
ReadLn(FirstName);
Write('Enter Last name: ');
ReadLn(LastName);
Person := TPerson.Create(FirstName, LastName);
Fullname := Person.GetFname();
Writeln('Full Name of the Person is ', Fullname);
Person.Destroy;
Writeln('Referencing object after calling destructor');
Fullname := Person.GetFname();
Writeln('Full Name of the Person is ', Fullname);
end
.